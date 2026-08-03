package com.portfoliomanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One day's worth of consolidated portfolio value - powers the performance chart. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshot {
    private Long id;
    private LocalDate snapshotDate;
    private BigDecimal totalInvestedBase;
    private BigDecimal totalValueBase;
    private BigDecimal realizedPlBase;
    private BigDecimal unrealizedPlBase;
    private LocalDateTime createdAt;
}
