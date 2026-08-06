package com.portfoliomanager.service;

import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.dto.PerformancePointResponse;
import com.portfoliomanager.model.PortfolioSnapshot;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.PortfolioSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Records a daily snapshot of the consolidated portfolio value so the customer
 * can see
 * a performance chart over time, and rolls today's value into "previous value"
 * so that
 * tomorrow's dashboard can correctly compute "today's gain/loss".
 */
@Service
public class SnapshotService {

        private final DashboardService dashboardService;
        private final PortfolioSnapshotRepository snapshotRepository;
        private final InvestmentRepository investmentRepository;
        private final CurrentUserService currentUserService;

        public SnapshotService(DashboardService dashboardService,
                        PortfolioSnapshotRepository snapshotRepository,
                        InvestmentRepository investmentRepository,
                        CurrentUserService currentUserService) {
                this.dashboardService = dashboardService;
                this.snapshotRepository = snapshotRepository;
                this.investmentRepository = investmentRepository;
                this.currentUserService = currentUserService;
        }

        /**
         * Take (or refresh) today's snapshot on demand - also triggered automatically
         * once a day.
         */
        public PortfolioSnapshot captureToday() {
                Long userId = currentUserService.getCurrentUserId();
                DashboardResponse dashboard = dashboardService.getDashboard();

                PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                                .snapshotDate(LocalDate.now())
                                .totalInvestedBase(dashboard.getTotalInvested())
                                .totalValueBase(dashboard.getTotalCurrentValue())
                                .realizedPlBase(dashboard.getRealizedPl())
                                .unrealizedPlBase(dashboard.getUnrealizedPl())
                                .build();

                snapshotRepository.upsert(userId, snapshot);
                investmentRepository.rollCurrentValueIntoPrevious(userId);
                return snapshot;
        }

        public List<PerformancePointResponse> getHistory(LocalDate from, LocalDate to) {
                Long userId = currentUserService.getCurrentUserId();
                List<PortfolioSnapshot> snapshots = (from != null && to != null)
                                ? snapshotRepository.findBetween(userId, from, to)
                                : snapshotRepository.findAll(userId);

                return snapshots.stream()
                                .map(s -> PerformancePointResponse.builder()
                                                .date(s.getSnapshotDate())
                                                .totalInvestedBase(s.getTotalInvestedBase())
                                                .totalValueBase(s.getTotalValueBase())
                                                .overallPlBase(s.getTotalValueBase().subtract(s.getTotalInvestedBase())
                                                                .add(s.getRealizedPlBase()))
                                                .build())
                                .toList();
        }
}
