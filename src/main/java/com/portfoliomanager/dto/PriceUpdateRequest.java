package com.portfoliomanager.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Used to refresh the market value of an investment.
 *  - For STOCK/ETF: supply currentPrice, currentValue is recalculated as quantity * currentPrice.
 *  - For FD/CASH: supply currentValue directly.
 */
@Data
public class PriceUpdateRequest {

    @Positive
    private BigDecimal currentPrice;

    @Positive
    private BigDecimal currentValue;
}
