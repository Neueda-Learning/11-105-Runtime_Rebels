package com.portfoliomanager.dto;

import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.CommodityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for creating an investment. Different fields matter for different types:
 *  - STOCK / ETF: quantity, avgBuyPrice, currentPrice
 *  - FD:          investedAmount (principal), currentValue, interestRate, maturityDate
 *  - CASH:        investedAmount, currentValue (usually equal)
 *  - COMMODITY:   quantity, avgBuyPrice, currentPrice, market, commodityType, purchaseDate
 */
@Data
public class InvestmentRequest {

    @NotNull
    private InvestmentType type;

    @NotBlank
    private String symbol;

    @NotBlank
    private String name;

    @NotBlank
    private String country;

    @NotBlank
    private String currency;

    @Positive
    private BigDecimal quantity;

    @Positive
    private BigDecimal avgBuyPrice;

    @Positive
    private BigDecimal currentPrice;

    @PositiveOrZero
    private BigDecimal investedAmount;

    @PositiveOrZero
    private BigDecimal currentValue;

    private BigDecimal interestRate;
    private LocalDate maturityDate;
    private LocalDate purchaseDate;
    private CommodityType commodityType;
    private String market;
    private String notes;
}
