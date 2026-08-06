package com.portfoliomanager.service;

import com.portfoliomanager.dto.AllocationItem;
import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds the single consolidated dashboard the customer asked for: total
 * invested,
 * current value, overall P/L, % return, today's gain/loss, realized vs
 * unrealized P/L,
 * and asset allocation breakdowns - all converted into the customer's base
 * currency.
 */
@Service
public class DashboardService {

    private final InvestmentRepository investmentRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyService currencyService;
    private final MilestoneService milestoneService;
    private final CurrentUserService currentUserService;

    public DashboardService(InvestmentRepository investmentRepository,
            TransactionRepository transactionRepository,
            CurrencyService currencyService,
            MilestoneService milestoneService,
            CurrentUserService currentUserService) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
        this.currencyService = currencyService;
        this.milestoneService = milestoneService;
        this.currentUserService = currentUserService;
    }

    public DashboardResponse getDashboard() {
        Long userId = currentUserService.getCurrentUserId();
        String baseCurrency = currencyService.getBaseCurrency();
        List<Investment> activeInvestments = investmentRepository.findAllActive(userId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        BigDecimal totalPreviousValue = BigDecimal.ZERO;

        for (Investment inv : activeInvestments) {
            totalInvested = totalInvested.add(currencyService.toBase(inv.getInvestedAmount(), inv.getCurrency()));
            totalCurrentValue = totalCurrentValue.add(currencyService.toBase(inv.getCurrentValue(), inv.getCurrency()));
            BigDecimal prev = inv.getPreviousValue() != null ? inv.getPreviousValue() : inv.getCurrentValue();
            totalPreviousValue = totalPreviousValue.add(currencyService.toBase(prev, inv.getCurrency()));
        }

        BigDecimal realizedPl = calculateRealizedPl(userId);
        BigDecimal unrealizedPl = totalCurrentValue.subtract(totalInvested);
        BigDecimal overallPl = unrealizedPl.add(realizedPl);
        BigDecimal overallPlPercent = percentOf(totalInvested, overallPl);

        BigDecimal todayGainLoss = totalCurrentValue.subtract(totalPreviousValue);
        BigDecimal todayGainLossPercent = percentOf(totalPreviousValue, todayGainLoss);

        milestoneService.refreshAchievedStatus(totalCurrentValue);

        return DashboardResponse.builder()
                .baseCurrency(baseCurrency)
                .totalInvested(scaled(totalInvested))
                .totalCurrentValue(scaled(totalCurrentValue))
                .unrealizedPl(scaled(unrealizedPl))
                .realizedPl(scaled(realizedPl))
                .overallPl(scaled(overallPl))
                .overallPlPercent(overallPlPercent)
                .todayGainLoss(scaled(todayGainLoss))
                .todayGainLossPercent(todayGainLossPercent)
                .allocationByType(allocationByType(activeInvestments, totalCurrentValue))
                .allocationByCountry(allocationBy(activeInvestments, Investment::getCountry, totalCurrentValue))
                .allocationByCurrency(allocationBy(activeInvestments, Investment::getCurrency, totalCurrentValue))
                .nextMilestone(milestoneService.findNext(totalCurrentValue).orElse(null))
                .achievedMilestoneCount(milestoneService.countAchieved())
                .build();
    }

    private BigDecimal calculateRealizedPl(Long userId) {
        List<Transaction> sells = transactionRepository.findAllRealizedPlTransactions(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction tx : sells) {
            if (tx.getRealizedPl() != null) {
                total = total.add(currencyService.toBase(tx.getRealizedPl(), tx.getCurrency()));
            }
        }
        return total;
    }

    private List<AllocationItem> allocationByType(List<Investment> investments, BigDecimal totalBase) {
        return allocationBy(investments, inv -> inv.getType().name(), totalBase);
    }

    private List<AllocationItem> allocationBy(List<Investment> investments,
            java.util.function.Function<Investment, String> classifier,
            BigDecimal totalBase) {
        Map<String, BigDecimal> grouped = investments.stream().collect(Collectors.groupingBy(
                classifier,
                Collectors.reducing(BigDecimal.ZERO,
                        inv -> currencyService.toBase(inv.getCurrentValue(), inv.getCurrency()),
                        BigDecimal::add)));

        return grouped.entrySet().stream()
                .map(e -> AllocationItem.builder()
                        .label(e.getKey())
                        .valueBase(scaled(e.getValue()))
                        .percentage(percentOf(totalBase, e.getValue()))
                        .build())
                .sorted(Comparator.comparing(AllocationItem::getValueBase).reversed())
                .toList();
    }

    private BigDecimal percentOf(BigDecimal base, BigDecimal part) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.divide(base, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaled(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
