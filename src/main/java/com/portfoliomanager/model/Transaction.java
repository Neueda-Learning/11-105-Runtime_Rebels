package com.portfoliomanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A single buy/sell/deposit/withdraw/interest event recorded against an investment. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private Long id;
    private Long investmentId;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal realizedPl;
    private String currency;
    private LocalDate transactionDate;
    private String notes;
    private LocalDateTime createdAt;
}
