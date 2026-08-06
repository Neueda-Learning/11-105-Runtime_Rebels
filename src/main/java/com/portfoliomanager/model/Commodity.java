package com.portfoliomanager.model;

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
public class Commodity {
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

