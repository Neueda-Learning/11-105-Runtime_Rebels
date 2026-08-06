package com.portfoliomanager.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single investment holding/position - a stock, an ETF, a fixed
 * deposit,
 * or a cash balance. This is the core object the customer manages from one
 * place.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investment {

    private Long id;

    @NotNull(message = "Investment type is required")
    private InvestmentType type;

    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;

    @NotBlank(message = "Investment name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 3, message = "Country code must not exceed 3 characters")
    private String country;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    // Applicable to COMMODITY
    private String market;
    private CommodityType commodityType;

    // Applicable mainly to STOCK / ETF
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Average buy price must be greater than 0")
    private BigDecimal avgBuyPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "Current price must be greater than 0")
    private BigDecimal currentPrice;

    // Universal fields used across all investment types (instrument currency)
    @NotNull(message = "Invested amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Invested amount must be greater than 0")
    private BigDecimal investedAmount;

    @DecimalMin(value = "0.0", message = "Current value must be non-negative")
    private BigDecimal currentValue;

    @DecimalMin(value = "0.0", message = "Previous value must be non-negative")
    private BigDecimal previousValue;

    // Applicable mainly to FD
    @DecimalMin(value = "0.0", message = "Interest rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Interest rate must not exceed 100")
    private BigDecimal interestRate;

    private LocalDate maturityDate;

    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date must not be in the future")
    private LocalDate purchaseDate;

    @NotNull(message = "Investment status is required")
    private InvestmentStatus status;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
