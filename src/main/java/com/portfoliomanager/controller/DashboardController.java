package com.portfoliomanager.controller;

import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.dto.PerformancePointResponse;
import com.portfoliomanager.model.PortfolioSnapshot;
import com.portfoliomanager.service.DashboardService;
import com.portfoliomanager.service.SnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * The single dashboard endpoint the customer asked for - a quick daily wealth overview
 * with everything needed to understand their financial position at a glance, plus a
 * performance-history endpoint to power a simple chart.
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Consolidated portfolio overview, performance history and daily wealth snapshot")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SnapshotService snapshotService;

    public DashboardController(DashboardService dashboardService, SnapshotService snapshotService) {
        this.dashboardService = dashboardService;
        this.snapshotService = snapshotService;
    }

    @Operation(summary = "Get the consolidated dashboard: totals, P/L, today's gain/loss, allocation breakdown, milestones")
    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    @Operation(summary = "Get portfolio performance history (for charting), optionally bounded by a date range")
    @GetMapping("/performance")
    public List<PerformancePointResponse> getPerformance(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return snapshotService.getHistory(from, to);
    }

    @Operation(summary = "Manually trigger today's wealth snapshot (normally runs automatically once a day)")
    @PostMapping("/snapshot")
    public PortfolioSnapshot captureSnapshot() {
        return snapshotService.captureToday();
    }
}
