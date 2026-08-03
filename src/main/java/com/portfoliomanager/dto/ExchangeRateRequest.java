package com.portfoliomanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateRequest {

    @NotNull
    @Positive
    private BigDecimal rateToBase;
}
