package com.portfoliomanager.service;

import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.dto.PerformancePointResponse;
import com.portfoliomanager.model.PortfolioSnapshot;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.PortfolioSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private SnapshotService snapshotService;

    @Test
    void captureToday_buildsSnapshotUpsertsAndRollsCurrentValue() {
        DashboardResponse dashboard = DashboardResponse.builder()
                .totalInvested(new BigDecimal("1000.00"))
                .totalCurrentValue(new BigDecimal("1200.00"))
                .realizedPl(new BigDecimal("50.00"))
                .unrealizedPl(new BigDecimal("200.00"))
                .build();
        when(dashboardService.getDashboard()).thenReturn(dashboard);

        PortfolioSnapshot result = snapshotService.captureToday();

        assertEquals(LocalDate.now(), result.getSnapshotDate());
        assertEquals(new BigDecimal("1000.00"), result.getTotalInvestedBase());
        assertEquals(new BigDecimal("1200.00"), result.getTotalValueBase());
        assertEquals(new BigDecimal("50.00"), result.getRealizedPlBase());
        assertEquals(new BigDecimal("200.00"), result.getUnrealizedPlBase());

        ArgumentCaptor<PortfolioSnapshot> captor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository).upsert(captor.capture());
        PortfolioSnapshot upserted = captor.getValue();
        assertEquals(result.getSnapshotDate(), upserted.getSnapshotDate());
        assertEquals(result.getTotalInvestedBase(), upserted.getTotalInvestedBase());
        assertEquals(result.getTotalValueBase(), upserted.getTotalValueBase());
        assertEquals(result.getRealizedPlBase(), upserted.getRealizedPlBase());
        assertEquals(result.getUnrealizedPlBase(), upserted.getUnrealizedPlBase());

        verify(investmentRepository).rollCurrentValueIntoPrevious();
    }

    @Test
    void getHistory_withRange_usesFindBetweenAndComputesOverallPl() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 5);

        when(snapshotRepository.findBetween(from, to)).thenReturn(List.of(
                PortfolioSnapshot.builder()
                        .snapshotDate(LocalDate.of(2026, 8, 1))
                        .totalInvestedBase(new BigDecimal("1000.00"))
                        .totalValueBase(new BigDecimal("1100.00"))
                        .realizedPlBase(new BigDecimal("20.00"))
                        .build(),
                PortfolioSnapshot.builder()
                        .snapshotDate(LocalDate.of(2026, 8, 2))
                        .totalInvestedBase(new BigDecimal("1000.00"))
                        .totalValueBase(new BigDecimal("950.00"))
                        .realizedPlBase(new BigDecimal("0.00"))
                        .build()));

        List<PerformancePointResponse> history = snapshotService.getHistory(from, to);

        assertEquals(2, history.size());
        assertEquals(new BigDecimal("120.00"), history.get(0).getOverallPlBase());
        assertEquals(new BigDecimal("-50.00"), history.get(1).getOverallPlBase());
    }

    @Test
    void getHistory_withoutRange_usesFindAll() {
        PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                .snapshotDate(LocalDate.of(2026, 8, 3))
                .totalInvestedBase(new BigDecimal("2000.00"))
                .totalValueBase(new BigDecimal("2100.00"))
                .realizedPlBase(new BigDecimal("10.00"))
                .build();

        when(snapshotRepository.findAll()).thenReturn(List.of(snapshot));

        List<PerformancePointResponse> history = snapshotService.getHistory(null, null);

        assertEquals(1, history.size());
        assertEquals(LocalDate.of(2026, 8, 3), history.get(0).getDate());
        assertEquals(new BigDecimal("110.00"), history.get(0).getOverallPlBase());
        verify(snapshotRepository).findAll();
    }
}
