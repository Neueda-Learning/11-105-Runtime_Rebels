package com.portfoliomanager.dto;

import com.portfoliomanager.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long investmentId;
    private String investmentSymbol;
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
