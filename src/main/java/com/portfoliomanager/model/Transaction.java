package com.portfoliomanager.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A single buy/sell/deposit/withdraw/interest event recorded against an
 * investment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private Long id;

    @NotNull(message = "Investment ID is required")
    private Long investmentId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private BigDecimal realizedPl;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date must not be in the future")
    private LocalDate transactionDate;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private LocalDateTime createdAt;
}
