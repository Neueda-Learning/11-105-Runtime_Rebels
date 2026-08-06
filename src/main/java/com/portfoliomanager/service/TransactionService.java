package com.portfoliomanager.service;

import com.portfoliomanager.dto.TransactionRequest;
import com.portfoliomanager.dto.TransactionResponse;
import com.portfoliomanager.exception.InvalidOperationException;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Records BUY/SELL/DEPOSIT/WITHDRAW/INTEREST transactions and keeps the parent
 * investment's quantity, average cost, invested amount and current value in
 * sync -
 * this is also where realized profit/loss is captured (on SELL), satisfying the
 * customer's requirement to see realized vs unrealized P/L separately.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final InvestmentRepository investmentRepository;
    private final InvestmentService investmentService;
    private final CurrentUserService currentUserService;

    public TransactionService(TransactionRepository transactionRepository,
            InvestmentRepository investmentRepository,
            InvestmentService investmentService,
            CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.investmentRepository = investmentRepository;
        this.investmentService = investmentService;
        this.currentUserService = currentUserService;
    }

    public List<TransactionResponse> findByInvestment(Long investmentId) {
        Long userId = currentUserService.getCurrentUserId();
        Investment investment = investmentService.getOrThrow(investmentId);
        return transactionRepository.findByInvestmentId(userId, investmentId).stream()
                .map(tx -> toResponse(tx, investment.getSymbol()))
                .toList();
    }

    public List<TransactionResponse> findAll() {
        Long userId = currentUserService.getCurrentUserId();
        List<Transaction> all = transactionRepository.findAll(userId);
        return all.stream()
                .map(tx -> {
                    String symbol = investmentRepository.findById(userId, tx.getInvestmentId())
                            .map(Investment::getSymbol).orElse(null);
                    return toResponse(tx, symbol);
                })
                .toList();
    }

    @Transactional
    public TransactionResponse record(Long investmentId, TransactionRequest request) {
        Investment investment = investmentService.getOrThrow(investmentId);
        TransactionType transactionType = request.getType();

        Transaction.TransactionBuilder txBuilder = Transaction.builder()
                .investmentId(investmentId)
                .type(transactionType)
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .amount(request.getAmount())
                .currency(investment.getCurrency())
                .transactionDate(request.getTransactionDate())
                .notes(request.getNotes());

        if (transactionType == TransactionType.BUY) {
            applyBuy(investment, request);
        } else if (transactionType == TransactionType.SELL) {
            txBuilder.realizedPl(applySell(investment, request));
        } else if (transactionType == TransactionType.DEPOSIT) {
            applyDeposit(investment, request);
        } else if (transactionType == TransactionType.WITHDRAW) {
            applyWithdraw(investment, request);
        } else if (transactionType == TransactionType.INTEREST) {
            applyInterest(investment, request);
        } else {
            throw new InvalidOperationException("Unsupported transaction type: " + transactionType);
        }

        investmentRepository.update(currentUserService.getCurrentUserId(), investment);
        Transaction saved = transactionRepository.save(txBuilder.build());
        return toResponse(saved, investment.getSymbol());
    }

    private void applyBuy(Investment investment, TransactionRequest request) {
        requireStockOrEtf(investment, "BUY");
        BigDecimal qty = requirePositive(request.getQuantity(), "quantity");
        BigDecimal price = requirePositive(request.getPrice(), "price");

        BigDecimal oldQty = nz(investment.getQuantity());
        BigDecimal oldAvg = nz(investment.getAvgBuyPrice());
        BigDecimal newQty = oldQty.add(qty);

        BigDecimal newAvg = oldQty.multiply(oldAvg).add(qty.multiply(price))
                .divide(newQty, 6, RoundingMode.HALF_UP);

        investment.setQuantity(newQty);
        investment.setAvgBuyPrice(newAvg);
        investment.setInvestedAmount(newQty.multiply(newAvg));
        investment.setCurrentPrice(price); // treat latest buy price as the freshest known price
        investment.setCurrentValue(newQty.multiply(price));
        investment.setStatus(InvestmentStatus.ACTIVE);
    }

    private BigDecimal applySell(Investment investment, TransactionRequest request) {
        requireStockOrEtf(investment, "SELL");
        BigDecimal qty = requirePositive(request.getQuantity(), "quantity");
        BigDecimal price = requirePositive(request.getPrice(), "price");

        BigDecimal oldQty = nz(investment.getQuantity());
        if (qty.compareTo(oldQty) > 0) {
            throw new InvalidOperationException(
                    "Cannot sell " + qty + " units - only " + oldQty + " units currently held");
        }

        BigDecimal avg = nz(investment.getAvgBuyPrice());
        BigDecimal realizedPl = qty.multiply(price.subtract(avg)).setScale(4, RoundingMode.HALF_UP);

        BigDecimal newQty = oldQty.subtract(qty);
        investment.setQuantity(newQty);
        investment.setInvestedAmount(newQty.multiply(avg));
        investment.setCurrentPrice(price);
        investment.setCurrentValue(newQty.multiply(price));
        if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            investment.setStatus(InvestmentStatus.CLOSED);
        }
        return realizedPl;
    }

    private void applyDeposit(Investment investment, TransactionRequest request) {
        requireType(investment, InvestmentType.CASH, "DEPOSIT");
        BigDecimal amount = requirePositive(request.getAmount(), "amount");
        investment.setInvestedAmount(nz(investment.getInvestedAmount()).add(amount));
        investment.setCurrentValue(nz(investment.getCurrentValue()).add(amount));
    }

    private void applyWithdraw(Investment investment, TransactionRequest request) {
        requireType(investment, InvestmentType.CASH, "WITHDRAW");
        BigDecimal amount = requirePositive(request.getAmount(), "amount");
        if (amount.compareTo(nz(investment.getCurrentValue())) > 0) {
            throw new InvalidOperationException("Cannot withdraw more than the current cash balance");
        }
        investment.setInvestedAmount(nz(investment.getInvestedAmount()).subtract(amount).max(BigDecimal.ZERO));
        investment.setCurrentValue(nz(investment.getCurrentValue()).subtract(amount));
    }

    private void applyInterest(Investment investment, TransactionRequest request) {
        requireType(investment, InvestmentType.FD, "INTEREST");
        BigDecimal amount = requirePositive(request.getAmount(), "amount");
        investment.setCurrentValue(nz(investment.getCurrentValue()).add(amount));
    }

    private void requireStockOrEtf(Investment investment, String op) {
        if (investment.getType() != InvestmentType.STOCK && investment.getType() != InvestmentType.ETF) {
            throw new InvalidOperationException(op + " transactions are only valid for STOCK/ETF investments");
        }
    }

    private void requireType(Investment investment, InvestmentType type, String op) {
        if (investment.getType() != type) {
            throw new InvalidOperationException(op + " transactions are only valid for " + type + " investments");
        }
    }

    private BigDecimal requirePositive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(
                    field + " must be provided and greater than zero for this transaction type");
        }
        return value;
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private TransactionResponse toResponse(Transaction tx, String symbol) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .investmentId(tx.getInvestmentId())
                .investmentSymbol(symbol)
                .type(tx.getType())
                .quantity(tx.getQuantity())
                .price(tx.getPrice())
                .amount(tx.getAmount())
                .realizedPl(tx.getRealizedPl())
                .currency(tx.getCurrency())
                .transactionDate(tx.getTransactionDate())
                .notes(tx.getNotes())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
