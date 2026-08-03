package com.portfoliomanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneResponse {
    private Long id;
    private String name;
    private BigDecimal thresholdValueBase;
    private String comparisonLabel;
    private boolean achieved;
    private LocalDate achievedDate;
    private BigDecimal progressPercent;
}
