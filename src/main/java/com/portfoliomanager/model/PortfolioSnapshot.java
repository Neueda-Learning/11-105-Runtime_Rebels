package com.portfoliomanager.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One day's worth of consolidated portfolio value - powers the performance
 * chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshot {
    private Long id;

    @NotNull(message = "Snapshot date is required")
    @PastOrPresent(message = "Snapshot date must not be in the future")
    private LocalDate snapshotDate;

    @NotNull(message = "Total invested amount is required")
    @DecimalMin(value = "0.0", message = "Total invested amount must be non-negative")
    private BigDecimal totalInvestedBase;

    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.0", message = "Total value must be non-negative")
    private BigDecimal totalValueBase;

    private BigDecimal realizedPlBase;
    private BigDecimal unrealizedPlBase;
    private LocalDateTime createdAt;
}
