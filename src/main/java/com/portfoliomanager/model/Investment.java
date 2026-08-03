package com.portfoliomanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single investment holding/position - a stock, an ETF, a fixed deposit,
 * or a cash balance. This is the core object the customer manages from one place.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investment {

    private Long id;
    private InvestmentType type;
    private String symbol;
    private String name;
    private String country;
    private String currency;

    // Applicable mainly to STOCK / ETF
    private BigDecimal quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentPrice;

    // Universal fields used across all investment types (instrument currency)
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal previousValue;

    // Applicable mainly to FD
    private BigDecimal interestRate;
    private LocalDate maturityDate;

    private LocalDate purchaseDate;
    private InvestmentStatus status;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
