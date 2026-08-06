package com.portfoliomanager.service;

import com.portfoliomanager.dto.InvestmentRequest;
import com.portfoliomanager.dto.InvestmentResponse;
import com.portfoliomanager.dto.PriceUpdateRequest;
import com.portfoliomanager.exception.InvalidOperationException;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.repository.InvestmentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final CurrencyService currencyService;
    private final CurrentUserService currentUserService;

    public InvestmentService(InvestmentRepository investmentRepository,
            CurrencyService currencyService,
            CurrentUserService currentUserService) {
        this.investmentRepository = investmentRepository;
        this.currencyService = currencyService;
        this.currentUserService = currentUserService;
    }

    public List<InvestmentResponse> findAll(InvestmentType type, String country, InvestmentStatus status) {
        return investmentRepository.findAll(currentUserService.getCurrentUserId(), type, country, status).stream()
                .map(this::toResponse)
                .toList();
    }

    public InvestmentResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public Investment getOrThrow(Long id) {
        return investmentRepository.findById(currentUserService.getCurrentUserId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found with id: " + id));
    }

    public InvestmentResponse create(InvestmentRequest request) {
        validateForType(request);
        Long userId = currentUserService.getCurrentUserId();
        String normalizedSymbol = normalizeSymbol(request.getSymbol());
        String normalizedCurrency = request.getCurrency().toUpperCase(Locale.ROOT);

        if (request.getType() == InvestmentType.STOCK || request.getType() == InvestmentType.ETF) {
            Investment merged = investmentRepository
                    .findActiveBySymbolAndType(userId, normalizedSymbol, request.getType())
                    .map(existing -> mergeIntoExisting(existing, request, normalizedSymbol, normalizedCurrency, userId))
                    .orElse(null);
            if (merged != null) {
                return toResponse(merged);
            }
        }

        BigDecimal quantity = request.getQuantity();
        BigDecimal avgBuyPrice = request.getAvgBuyPrice();
        BigDecimal currentPrice = request.getCurrentPrice();

        BigDecimal investedAmount;
        BigDecimal currentValue;

        if (request.getType() == InvestmentType.STOCK || request.getType() == InvestmentType.ETF) {
            investedAmount = quantity.multiply(avgBuyPrice);
            currentValue = quantity.multiply(currentPrice != null ? currentPrice : avgBuyPrice);
            if (currentPrice == null) {
                currentPrice = avgBuyPrice;
            }
        } else {
            // FD / CASH: caller supplies invested/current amounts directly
            investedAmount = request.getInvestedAmount();
            currentValue = request.getCurrentValue() != null ? request.getCurrentValue() : request.getInvestedAmount();
        }

        Investment investment = Investment.builder()
                .type(request.getType())
            .symbol(normalizedSymbol)
                .name(request.getName())
                .country(request.getCountry())
            .currency(normalizedCurrency)
                .quantity(quantity)
                .avgBuyPrice(avgBuyPrice)
                .currentPrice(currentPrice)
                .investedAmount(investedAmount)
                .currentValue(currentValue)
                .previousValue(currentValue)
                .interestRate(request.getInterestRate())
                .maturityDate(request.getMaturityDate())
                .purchaseDate(request.getPurchaseDate())
                .status(InvestmentStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        return toResponse(investmentRepository.save(userId, investment));
    }

    public InvestmentResponse update(Long id, InvestmentRequest request) {
        validateForType(request);
        Investment existing = getOrThrow(id);

        existing.setName(request.getName());
        existing.setCountry(request.getCountry());
        existing.setCurrency(request.getCurrency().toUpperCase());
        existing.setNotes(request.getNotes());
        existing.setMaturityDate(request.getMaturityDate());
        existing.setInterestRate(request.getInterestRate());
        if (request.getPurchaseDate() != null) {
            existing.setPurchaseDate(request.getPurchaseDate());
        }
        // NOTE: quantity/avgBuyPrice/currentValue are intentionally NOT changed here -
        // they are only ever changed via transactions (buy/sell/deposit/etc.) or the
        // dedicated price-refresh endpoint, so the audit trail always stays consistent.

        return toResponse(investmentRepository.update(currentUserService.getCurrentUserId(), existing));
    }

    public InvestmentResponse updatePrice(Long id, PriceUpdateRequest request) {
        Investment investment = getOrThrow(id);

        BigDecimal newCurrentPrice = investment.getCurrentPrice();
        BigDecimal newCurrentValue;

        if (investment.getType() == InvestmentType.STOCK || investment.getType() == InvestmentType.ETF) {
            if (request.getCurrentPrice() == null) {
                throw new InvalidOperationException("currentPrice is required for STOCK/ETF price updates");
            }
            newCurrentPrice = request.getCurrentPrice();
            newCurrentValue = investment.getQuantity().multiply(newCurrentPrice);
        } else {
            if (request.getCurrentValue() == null) {
                throw new InvalidOperationException("currentValue is required for FD/CASH price updates");
            }
            newCurrentValue = request.getCurrentValue();
        }

        investmentRepository.updatePrice(currentUserService.getCurrentUserId(), id, newCurrentPrice, newCurrentValue);
        return toResponse(getOrThrow(id));
    }

    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        if (!investmentRepository.existsById(userId, id)) {
            throw new ResourceNotFoundException("Investment not found with id: " + id);
        }
        investmentRepository.deleteById(userId, id);
    }

    private void validateForType(InvestmentRequest request) {
        if (request.getType() == InvestmentType.STOCK || request.getType() == InvestmentType.ETF) {
            if (request.getQuantity() == null || request.getAvgBuyPrice() == null) {
                throw new InvalidOperationException("quantity and avgBuyPrice are required for STOCK/ETF investments");
            }
        } else {
            if (request.getInvestedAmount() == null) {
                throw new InvalidOperationException("investedAmount is required for FD/CASH investments");
            }
        }
    }

    private Investment mergeIntoExisting(Investment existing,
            InvestmentRequest request,
            String normalizedSymbol,
            String normalizedCurrency,
            Long userId) {
        BigDecimal quantity = request.getQuantity();
        BigDecimal avgBuyPrice = request.getAvgBuyPrice();
        BigDecimal currentPrice = request.getCurrentPrice() != null ? request.getCurrentPrice() : avgBuyPrice;

        BigDecimal oldQuantity = nz(existing.getQuantity());
        BigDecimal oldAverage = nz(existing.getAvgBuyPrice());
        BigDecimal newQuantity = oldQuantity.add(quantity);
        BigDecimal newAverage = oldQuantity.multiply(oldAverage)
                .add(quantity.multiply(avgBuyPrice))
                .divide(newQuantity, 6, RoundingMode.HALF_UP);
        BigDecimal newCurrentValue = newQuantity.multiply(currentPrice);

        existing.setSymbol(normalizedSymbol);
        existing.setName(request.getName());
        existing.setCountry(request.getCountry());
        existing.setCurrency(normalizedCurrency);
        existing.setQuantity(newQuantity);
        existing.setAvgBuyPrice(newAverage);
        existing.setCurrentPrice(currentPrice);
        existing.setInvestedAmount(newQuantity.multiply(newAverage));
        existing.setCurrentValue(newCurrentValue);
        existing.setStatus(InvestmentStatus.ACTIVE);
        existing.setNotes(request.getNotes());

        if (request.getPurchaseDate() != null) {
            existing.setPurchaseDate(request.getPurchaseDate());
        }
        if (existing.getPreviousValue() == null) {
            existing.setPreviousValue(newCurrentValue);
        }

        return investmentRepository.update(userId, existing);
    }

    private String normalizeSymbol(String symbol) {
        return symbol == null ? null : symbol.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public InvestmentResponse toResponse(Investment inv) {
        BigDecimal investedBase = currencyService.toBase(inv.getInvestedAmount(), inv.getCurrency());
        BigDecimal currentBase = currencyService.toBase(inv.getCurrentValue(), inv.getCurrency());

        BigDecimal unrealizedPl = inv.getCurrentValue().subtract(inv.getInvestedAmount());
        BigDecimal unrealizedPlPercent = percentChange(inv.getInvestedAmount(), unrealizedPl);

        return InvestmentResponse.builder()
                .id(inv.getId())
                .type(inv.getType())
                .symbol(inv.getSymbol())
                .name(inv.getName())
                .country(inv.getCountry())
                .currency(inv.getCurrency())
                .quantity(inv.getQuantity())
                .avgBuyPrice(inv.getAvgBuyPrice())
                .currentPrice(inv.getCurrentPrice())
                .investedAmount(inv.getInvestedAmount())
                .currentValue(inv.getCurrentValue())
                .previousValue(inv.getPreviousValue())
                .investedAmountBase(investedBase)
                .currentValueBase(currentBase)
                .unrealizedPl(unrealizedPl)
                .unrealizedPlPercent(unrealizedPlPercent)
                .interestRate(inv.getInterestRate())
                .maturityDate(inv.getMaturityDate())
                .purchaseDate(inv.getPurchaseDate())
                .status(inv.getStatus())
                .notes(inv.getNotes())
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }

    static BigDecimal percentChange(BigDecimal base, BigDecimal change) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return change.divide(base, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
