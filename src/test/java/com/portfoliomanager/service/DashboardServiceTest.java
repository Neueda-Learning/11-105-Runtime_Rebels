package com.portfoliomanager.service;

import com.portfoliomanager.dto.AllocationItem;
import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.dto.MilestoneResponse;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

        private static final Long USER_ID = 1L;

    @Mock
    private InvestmentRepository investmentRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private MilestoneService milestoneService;
        @Mock
        private CurrentUserService currentUserService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboard_computesAggregatesAndAllocationsAcrossCurrencies() {
        Investment usStock = Investment.builder()
                .type(InvestmentType.STOCK)
                .country("US")
                .currency("USD")
                .investedAmount(new BigDecimal("100.00"))
                .currentValue(new BigDecimal("120.00"))
                .previousValue(new BigDecimal("115.00"))
                .build();

        Investment inEtf = Investment.builder()
                .type(InvestmentType.ETF)
                .country("India")
                .currency("INR")
                .investedAmount(new BigDecimal("1000.00"))
                .currentValue(new BigDecimal("900.00"))
                .previousValue(null)
                .build();

        Transaction sellUsd = Transaction.builder()
                .currency("USD")
                .realizedPl(new BigDecimal("10.00"))
                .build();
        Transaction sellInr = Transaction.builder()
                .currency("INR")
                .realizedPl(new BigDecimal("200.00"))
                .build();
        Transaction sellNullRealized = Transaction.builder()
                .currency("USD")
                .realizedPl(null)
                .build();

        MilestoneResponse milestone = MilestoneResponse.builder()
                .id(7L)
                .name("Buy dream car")
                .thresholdValueBase(new BigDecimal("15000.00"))
                .comparisonLabel("Luxury Sedan")
                .achieved(false)
                .progressPercent(new BigDecimal("70.00"))
                .build();

        when(currencyService.getBaseCurrency()).thenReturn("INR");
        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(investmentRepository.findAllActive(USER_ID)).thenReturn(List.of(usStock, inEtf));
        when(transactionRepository.findAllRealizedPlTransactions(USER_ID))
                .thenReturn(List.of(sellUsd, sellInr, sellNullRealized));
        when(currencyService.toBase(any(BigDecimal.class), anyString()))
                .thenAnswer(invocation -> convertToInr(invocation.getArgument(0), invocation.getArgument(1)));
        when(milestoneService.findNext(new BigDecimal("10500.00"))).thenReturn(Optional.of(milestone));
        when(milestoneService.countAchieved()).thenReturn(2L);

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals("INR", response.getBaseCurrency());
        assertEquals(new BigDecimal("9000.00"), response.getTotalInvested());
        assertEquals(new BigDecimal("10500.00"), response.getTotalCurrentValue());
        assertEquals(new BigDecimal("1500.00"), response.getUnrealizedPl());
        assertEquals(new BigDecimal("1000.00"), response.getRealizedPl());
        assertEquals(new BigDecimal("2500.00"), response.getOverallPl());
        assertEquals(new BigDecimal("27.78"), response.getOverallPlPercent());
        assertEquals(new BigDecimal("400.00"), response.getTodayGainLoss());
        assertEquals(new BigDecimal("3.96"), response.getTodayGainLossPercent());
        assertEquals(2L, response.getAchievedMilestoneCount());
        assertEquals(milestone, response.getNextMilestone());

        Map<String, AllocationItem> byType = toMap(response.getAllocationByType());
        assertEquals(new BigDecimal("9600.00"), byType.get("STOCK").getValueBase());
        assertEquals(new BigDecimal("91.43"), byType.get("STOCK").getPercentage());
        assertEquals(new BigDecimal("900.00"), byType.get("ETF").getValueBase());
        assertEquals(new BigDecimal("8.57"), byType.get("ETF").getPercentage());

        // Allocation lists are sorted descending by base value.
        assertEquals("STOCK", response.getAllocationByType().get(0).getLabel());
        assertEquals("US", response.getAllocationByCountry().get(0).getLabel());
        assertEquals("USD", response.getAllocationByCurrency().get(0).getLabel());

        verify(milestoneService).refreshAchievedStatus(new BigDecimal("10500.00"));
    }

    @Test
    void getDashboard_handlesEmptyPortfolioAndZeroDivisionSafely() {
        when(currencyService.getBaseCurrency()).thenReturn("INR");
                when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
                when(investmentRepository.findAllActive(USER_ID)).thenReturn(List.of());
                when(transactionRepository.findAllRealizedPlTransactions(USER_ID)).thenReturn(List.of());
        when(milestoneService.findNext(BigDecimal.ZERO)).thenReturn(Optional.empty());
        when(milestoneService.countAchieved()).thenReturn(0L);

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(new BigDecimal("0.00"), response.getTotalInvested());
        assertEquals(new BigDecimal("0.00"), response.getTotalCurrentValue());
        assertEquals(new BigDecimal("0.00"), response.getUnrealizedPl());
        assertEquals(new BigDecimal("0.00"), response.getRealizedPl());
        assertEquals(new BigDecimal("0.00"), response.getOverallPl());
        assertEquals(BigDecimal.ZERO, response.getOverallPlPercent());
        assertEquals(new BigDecimal("0.00"), response.getTodayGainLoss());
        assertEquals(BigDecimal.ZERO, response.getTodayGainLossPercent());
        assertEquals(List.of(), response.getAllocationByType());
        assertEquals(List.of(), response.getAllocationByCountry());
        assertEquals(List.of(), response.getAllocationByCurrency());
        assertEquals(0L, response.getAchievedMilestoneCount());
        assertNull(response.getNextMilestone());

        verify(milestoneService).refreshAchievedStatus(BigDecimal.ZERO);
    }

    private static BigDecimal convertToInr(BigDecimal amount, String currency) {
        if ("USD".equals(currency)) {
            return amount.multiply(new BigDecimal("80"));
        }
        return amount;
    }

    private static Map<String, AllocationItem> toMap(List<AllocationItem> allocation) {
        return allocation.stream().collect(Collectors.toMap(AllocationItem::getLabel, Function.identity()));
    }
}
