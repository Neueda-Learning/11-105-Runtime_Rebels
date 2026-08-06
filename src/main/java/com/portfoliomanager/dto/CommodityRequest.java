package com.portfoliomanager.dto;

import com.portfoliomanager.model.CommodityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CommodityRequest {
    @NotBlank
    private String commodityName;

    @NotNull
    private CommodityType commodityType;

    @NotBlank
    private String marketExchange;

    @NotBlank
    private String country;

    @NotBlank
    private String currency;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotNull
    @Positive
    private BigDecimal purchasePrice;

    @NotNull
    @Positive
    private BigDecimal currentPrice;

    @NotNull
    private LocalDate purchaseDate;

    private String notes;
}

