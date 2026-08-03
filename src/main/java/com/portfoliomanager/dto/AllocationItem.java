package com.portfoliomanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One slice of the portfolio allocation breakdown (by type, country, or currency). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationItem {
    private String label;
    private BigDecimal valueBase;
    private BigDecimal percentage;
}
