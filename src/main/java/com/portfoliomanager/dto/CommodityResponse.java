package com.portfoliomanager.dto;

import com.portfoliomanager.model.CommodityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommodityResponse {
    private Long id;
    private Long investmentId;

    private String commodityName;
    private CommodityType commodityType;
    private String marketExchange;

    private String country;
    private String currency;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private BigDecimal currentPrice;
    private LocalDate purchaseDate;

    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

