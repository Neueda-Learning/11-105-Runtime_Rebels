package com.portfoliomanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * The single dashboard payload the customer asked for: a quick, at-a-glance view
 * of overall portfolio health, all expressed in the base/preferred currency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private String baseCurrency;

    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;

    private BigDecimal unrealizedPl;
    private BigDecimal realizedPl;
    private BigDecimal overallPl;
    private BigDecimal overallPlPercent;

    private BigDecimal todayGainLoss;
    private BigDecimal todayGainLossPercent;

    private List<AllocationItem> allocationByType;
    private List<AllocationItem> allocationByCountry;
    private List<AllocationItem> allocationByCurrency;

    private MilestoneResponse nextMilestone;
    private long achievedMilestoneCount;
}
