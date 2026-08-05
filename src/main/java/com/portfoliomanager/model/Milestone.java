package com.portfoliomanager.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A wealth-related, feel-good milestone, e.g. "portfolio crossed the price of a
 * luxury car".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Milestone {
    private Long id;

    @NotBlank(message = "Milestone name is required")
    @Size(max = 255, message = "Milestone name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Threshold value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Threshold value must be greater than 0")
    private BigDecimal thresholdValueBase;

    @NotBlank(message = "Comparison label is required")
    @Size(max = 255, message = "Comparison label must not exceed 255 characters")
    private String comparisonLabel;

    private boolean achieved;
    private LocalDate achievedDate;
    private LocalDateTime createdAt;
}
