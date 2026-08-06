package com.portfoliomanager.service;

import com.portfoliomanager.dto.CommodityRequest;
import com.portfoliomanager.dto.CommodityResponse;
import com.portfoliomanager.dto.InvestmentRequest;
import com.portfoliomanager.dto.InvestmentResponse;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Commodity;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.repository.CommodityRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CommodityService {

    private final CommodityRepository commodityRepository;
    private final InvestmentService investmentService;

    public CommodityService(CommodityRepository commodityRepository, InvestmentService investmentService) {
        this.commodityRepository = commodityRepository;
        this.investmentService = investmentService;
    }

    public CommodityResponse create(CommodityRequest request) {
        InvestmentResponse created = investmentService.create(toInvestmentRequest(request));
        Commodity commodity = commodityRepository.findByInvestmentId(created.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Commodity metadata not found for investment: " + created.getId()));
        return toResponse(commodity, created);
    }

    public List<CommodityResponse> findAll() {
        return commodityRepository.findAll().stream()
                .map(c -> toResponse(c, investmentService.findById(c.getInvestmentId())))
                .toList();
    }

    public CommodityResponse findById(Long id) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity not found with id: " + id));
        return toResponse(commodity, investmentService.findById(commodity.getInvestmentId()));
    }

    public CommodityResponse update(Long id, CommodityRequest request) {
        Commodity existing = commodityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity not found with id: " + id));

        investmentService.update(existing.getInvestmentId(), toInvestmentRequest(request));
        Commodity updated = commodityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity not found with id: " + id));
        return toResponse(updated, investmentService.findById(updated.getInvestmentId()));
    }

    public void delete(Long id) {
        Commodity existing = commodityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commodity not found with id: " + id));
        investmentService.delete(existing.getInvestmentId());
    }

    private InvestmentRequest toInvestmentRequest(CommodityRequest request) {
        InvestmentRequest inv = new InvestmentRequest();
        inv.setType(InvestmentType.COMMODITY);
        inv.setSymbol(request.getCommodityName());
        inv.setName(request.getCommodityName());
        inv.setCountry(request.getCountry());
        inv.setCurrency(request.getCurrency());
        inv.setQuantity(request.getQuantity());
        inv.setAvgBuyPrice(request.getPurchasePrice());
        inv.setCurrentPrice(request.getCurrentPrice());
        inv.setPurchaseDate(request.getPurchaseDate());
        inv.setMarket(request.getMarketExchange());
        inv.setCommodityType(request.getCommodityType());
        inv.setNotes(request.getNotes());
        return inv;
    }

    private CommodityResponse toResponse(Commodity commodity, InvestmentResponse investment) {
        BigDecimal invested = investment.getInvestedAmount();
        BigDecimal current = investment.getCurrentValue();
        BigDecimal pl = current.subtract(invested);
        BigDecimal plPercent = BigDecimal.ZERO;
        if (invested != null && invested.compareTo(BigDecimal.ZERO) > 0) {
            plPercent = pl.divide(invested, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return CommodityResponse.builder()
                .id(commodity.getId())
                .investmentId(commodity.getInvestmentId())
                .commodityName(commodity.getCommodityName())
                .commodityType(commodity.getCommodityType())
                .marketExchange(commodity.getMarketExchange())
                .country(commodity.getCountry())
                .currency(commodity.getCurrency())
                .quantity(commodity.getQuantity())
                .purchasePrice(commodity.getPurchasePrice())
                .currentPrice(commodity.getCurrentPrice())
                .purchaseDate(commodity.getPurchaseDate())
                .investedAmount(invested)
                .currentValue(current)
                .profitLoss(pl)
                .profitLossPercent(plPercent)
                .createdAt(commodity.getCreatedAt())
                .updatedAt(commodity.getUpdatedAt())
                .build();
    }
}

