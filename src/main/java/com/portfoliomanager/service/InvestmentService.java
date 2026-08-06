package com.portfoliomanager.service;

import com.portfoliomanager.dto.InvestmentRequest;
import com.portfoliomanager.dto.InvestmentResponse;
import com.portfoliomanager.dto.PriceUpdateRequest;
import com.portfoliomanager.exception.InvalidOperationException;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.Commodity;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.CommodityRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final CurrencyService currencyService;
    private final CommodityRepository commodityRepository;

    public InvestmentService(InvestmentRepository investmentRepository,
                             CurrencyService currencyService,
                             CommodityRepository commodityRepository) {
        this.investmentRepository = investmentRepository;
        this.currencyService = currencyService;
        this.commodityRepository = commodityRepository;
    }

    public List<InvestmentResponse> findAll(InvestmentType type, String country, InvestmentStatus status) {
        return investmentRepository.findAll(type, country, status).stream()
                .map(this::toResponse)
                .toList();
    }

    public InvestmentResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public Investment getOrThrow(Long id) {
        return investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found with id: " + id));
    }

    public InvestmentResponse create(InvestmentRequest request) {
        validateForType(request);

        BigDecimal quantity = request.getQuantity();
        BigDecimal avgBuyPrice = request.getAvgBuyPrice();
        BigDecimal currentPrice = request.getCurrentPrice();

        BigDecimal investedAmount;
        BigDecimal currentValue;

        if (isTradeableType(request.getType())) {
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
                .symbol(request.getSymbol())
                .name(request.getName())
                .country(request.getCountry())
                .currency(request.getCurrency().toUpperCase())
                .market(request.getMarket())
                .commodityType(request.getCommodityType())
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

        Investment saved = investmentRepository.save(investment);
        syncCommodityDetails(saved, request);
        return toResponse(investmentRepository.findById(saved.getId()).orElse(saved));
    }

    public InvestmentResponse update(Long id, InvestmentRequest request) {
        validateForType(request);
        Investment existing = getOrThrow(id);

        existing.setSymbol(request.getSymbol());
        existing.setName(request.getName());
        existing.setCountry(request.getCountry());
        existing.setCurrency(request.getCurrency().toUpperCase());
        existing.setMarket(request.getMarket());
        existing.setCommodityType(request.getCommodityType());
        existing.setNotes(request.getNotes());
        existing.setMaturityDate(request.getMaturityDate());
        existing.setInterestRate(request.getInterestRate());
        if (request.getPurchaseDate() != null) {
            existing.setPurchaseDate(request.getPurchaseDate());
        }
        if (existing.getType() == InvestmentType.COMMODITY) {
            existing.setQuantity(request.getQuantity());
            existing.setAvgBuyPrice(request.getAvgBuyPrice());
            existing.setCurrentPrice(request.getCurrentPrice() != null ? request.getCurrentPrice() : request.getAvgBuyPrice());
            existing.setInvestedAmount(existing.getQuantity().multiply(existing.getAvgBuyPrice()));
            existing.setCurrentValue(existing.getQuantity().multiply(existing.getCurrentPrice()));
            existing.setPreviousValue(existing.getCurrentValue());
        }

        Investment updated = investmentRepository.update(existing);
        syncCommodityDetails(updated, request);
        return toResponse(investmentRepository.findById(updated.getId()).orElse(updated));
    }

    public InvestmentResponse updatePrice(Long id, PriceUpdateRequest request) {
        Investment investment = getOrThrow(id);

        BigDecimal newCurrentPrice = investment.getCurrentPrice();
        BigDecimal newCurrentValue;

        if (isTradeableType(investment.getType())) {
            if (request.getCurrentPrice() == null) {
                throw new InvalidOperationException("currentPrice is required for STOCK/ETF/COMMODITY price updates");
            }
            newCurrentPrice = request.getCurrentPrice();
            newCurrentValue = investment.getQuantity().multiply(newCurrentPrice);
        } else {
            if (request.getCurrentValue() == null) {
                throw new InvalidOperationException("currentValue is required for FD/CASH price updates");
            }
            newCurrentValue = request.getCurrentValue();
        }

        investmentRepository.updatePrice(id, newCurrentPrice, newCurrentValue);
        return toResponse(getOrThrow(id));
    }

    public void delete(Long id) {
        if (!investmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Investment not found with id: " + id);
        }
        investmentRepository.deleteById(id);
    }

    private void validateForType(InvestmentRequest request) {
        if (isTradeableType(request.getType())) {
            if (request.getQuantity() == null || request.getAvgBuyPrice() == null) {
                throw new InvalidOperationException("quantity and avgBuyPrice are required for STOCK/ETF/COMMODITY investments");
            }
            if (request.getType() == InvestmentType.COMMODITY) {
                if (request.getCommodityType() == null || request.getMarket() == null || request.getMarket().isBlank()) {
                    throw new InvalidOperationException("commodityType and market are required for COMMODITY investments");
                }
                if (request.getPurchaseDate() == null) {
                    throw new InvalidOperationException("purchaseDate is required for COMMODITY investments");
                }
            }
        } else {
            if (request.getInvestedAmount() == null) {
                throw new InvalidOperationException("investedAmount is required for FD/CASH investments");
            }
        }
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
                .market(inv.getMarket())
                .commodityType(inv.getCommodityType())
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

    private boolean isTradeableType(InvestmentType type) {
        return type == InvestmentType.STOCK || type == InvestmentType.ETF || type == InvestmentType.COMMODITY;
    }

    private void syncCommodityDetails(Investment investment, InvestmentRequest request) {
        if (investment.getType() != InvestmentType.COMMODITY) {
            return;
        }

        Commodity commodity = Commodity.builder()
                .investmentId(investment.getId())
                .commodityName(investment.getName())
                .commodityType(request.getCommodityType())
                .marketExchange(request.getMarket())
                .country(investment.getCountry())
                .currency(investment.getCurrency())
                .quantity(investment.getQuantity())
                .purchasePrice(investment.getAvgBuyPrice())
                .currentPrice(investment.getCurrentPrice())
                .purchaseDate(investment.getPurchaseDate())
                .build();

        commodityRepository.upsertByInvestmentId(commodity);
    }

    static BigDecimal percentChange(BigDecimal base, BigDecimal change) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return change.divide(base, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
