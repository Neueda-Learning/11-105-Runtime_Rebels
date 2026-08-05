package com.portfoliomanager.controller;

import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.dto.PerformancePointResponse;
import com.portfoliomanager.model.PortfolioSnapshot;
import com.portfoliomanager.service.DashboardService;
import com.portfoliomanager.service.SnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private SnapshotService snapshotService;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(dashboardService, snapshotService);
    }

    @Test
    void getDashboard_returnsDashboardFromService() {
        DashboardResponse response = DashboardResponse.builder()
                .totalCurrentValue(new BigDecimal("12345"))
                .build();
        when(dashboardService.getDashboard()).thenReturn(response);

        DashboardResponse result = controller.getDashboard();

        assertSame(response, result);
        verify(dashboardService).getDashboard();
    }

    @Test
    void getPerformance_delegatesDateRangeToSnapshotService() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 8, 5);
        PerformancePointResponse point = PerformancePointResponse.builder().build();
        when(snapshotService.getHistory(from, to)).thenReturn(List.of(point));

        List<PerformancePointResponse> result = controller.getPerformance(from, to);

        assertEquals(1, result.size());
        assertSame(point, result.get(0));
        verify(snapshotService).getHistory(from, to);
    }

    @Test
    void captureSnapshot_returnsSnapshotFromService() {
        PortfolioSnapshot snapshot = PortfolioSnapshot.builder().id(1L).build();
        when(snapshotService.captureToday()).thenReturn(snapshot);

        PortfolioSnapshot result = controller.captureSnapshot();

        assertSame(snapshot, result);
        verify(snapshotService).captureToday();
    }
}
