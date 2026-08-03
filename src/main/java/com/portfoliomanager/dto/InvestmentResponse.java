package com.portfoliomanager.dto;

import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
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
public class InvestmentResponse {
    private Long id;
    private InvestmentType type;
    private String symbol;
    private String name;
    private String country;
    private String currency;

    private BigDecimal quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentPrice;

    private BigDecimal investedAmount;      // instrument currency
    private BigDecimal currentValue;        // instrument currency
    private BigDecimal previousValue;       // instrument currency

    private BigDecimal investedAmountBase;  // converted to base currency
    private BigDecimal currentValueBase;    // converted to base currency

    private BigDecimal unrealizedPl;        // instrument currency
    private BigDecimal unrealizedPlPercent;

    private BigDecimal interestRate;
    private LocalDate maturityDate;
    private LocalDate purchaseDate;

    private InvestmentStatus status;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
