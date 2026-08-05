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
import java.time.LocalDateTime;

/**
 * Conversion rate of 1 unit of currencyCode into the customer's base currency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    private Long id;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be a 3-letter ISO code")
    private String currencyCode;

    @NotNull(message = "Rate to base is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate to base must be greater than 0")
    private BigDecimal rateToBase;

    private LocalDateTime updatedAt;
}
