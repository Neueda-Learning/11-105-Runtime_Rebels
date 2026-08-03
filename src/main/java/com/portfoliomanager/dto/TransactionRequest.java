package com.portfoliomanager.dto;

import com.portfoliomanager.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload for recording a transaction against an investment.
 *  - BUY / SELL (STOCK, ETF): quantity + price required
 *  - DEPOSIT / WITHDRAW (CASH): amount required
 *  - INTEREST (FD): amount required (interest accrued)
 */
@Data
public class TransactionRequest {

    @NotNull
    private TransactionType type;

    private BigDecimal quantity;
    private BigDecimal price;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate transactionDate;

    private String notes;
}
