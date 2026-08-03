package com.portfoliomanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One point on the portfolio performance chart. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformancePointResponse {
    private LocalDate date;
    private BigDecimal totalInvestedBase;
    private BigDecimal totalValueBase;
    private BigDecimal overallPlBase;
}
