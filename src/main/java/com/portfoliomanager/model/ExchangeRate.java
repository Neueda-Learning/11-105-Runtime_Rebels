package com.portfoliomanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Conversion rate of 1 unit of currencyCode into the customer's base currency. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    private Long id;
    private String currencyCode;
    private BigDecimal rateToBase;
    private LocalDateTime updatedAt;
}
