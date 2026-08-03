package com.portfoliomanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A wealth-related, feel-good milestone, e.g. "portfolio crossed the price of a luxury car". */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Milestone {
    private Long id;
    private String name;
    private BigDecimal thresholdValueBase;
    private String comparisonLabel;
    private boolean achieved;
    private LocalDate achievedDate;
    private LocalDateTime createdAt;
}
