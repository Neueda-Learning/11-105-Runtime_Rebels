package com.portfoliomanager.service;

import com.portfoliomanager.dto.MilestoneRequest;
import com.portfoliomanager.dto.MilestoneResponse;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Milestone;
import com.portfoliomanager.repository.MilestoneRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Manages wealth-related, feel-good milestones the customer asked for - e.g.
 * comparing
 * the portfolio's current value against something aspirational like a luxury
 * car.
 */
@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final CurrentUserService currentUserService;

    public MilestoneService(MilestoneRepository milestoneRepository, CurrentUserService currentUserService) {
        this.milestoneRepository = milestoneRepository;
        this.currentUserService = currentUserService;
    }

    public List<MilestoneResponse> findAll(BigDecimal currentPortfolioValueBase) {
        return milestoneRepository.findAll(currentUserService.getCurrentUserId()).stream()
                .map(m -> toResponse(m, currentPortfolioValueBase))
                .toList();
    }

    public MilestoneResponse create(MilestoneRequest request) {
        Milestone milestone = Milestone.builder()
                .name(request.getName())
                .thresholdValueBase(request.getThresholdValueBase())
                .comparisonLabel(request.getComparisonLabel())
                .achieved(false)
                .build();
        return toResponse(milestoneRepository.save(currentUserService.getCurrentUserId(), milestone), BigDecimal.ZERO);
    }

    public void delete(Long id) {
        if (!milestoneRepository.deleteById(currentUserService.getCurrentUserId(), id)) {
            throw new ResourceNotFoundException("Milestone not found with id: " + id);
        }
    }

    /**
     * Marks any milestone whose threshold has now been crossed as achieved. Called
     * after dashboard recompute.
     */
    public void refreshAchievedStatus(BigDecimal currentPortfolioValueBase) {
        Long userId = currentUserService.getCurrentUserId();
        for (Milestone m : milestoneRepository.findAll(userId)) {
            if (!m.isAchieved() && currentPortfolioValueBase.compareTo(m.getThresholdValueBase()) >= 0) {
                milestoneRepository.markAchieved(userId, m.getId(), LocalDate.now());
            }
        }
    }

    /**
     * The next unachieved milestone (smallest threshold not yet reached) - useful
     * for a "how close am I" widget.
     */
    public Optional<MilestoneResponse> findNext(BigDecimal currentPortfolioValueBase) {
        return milestoneRepository.findAll(currentUserService.getCurrentUserId()).stream()
                .filter(m -> !m.isAchieved())
                .min(Comparator.comparing(Milestone::getThresholdValueBase))
                .map(m -> toResponse(m, currentPortfolioValueBase));
    }

    public long countAchieved() {
        return milestoneRepository.findAll(currentUserService.getCurrentUserId()).stream().filter(Milestone::isAchieved)
                .count();
    }

    private MilestoneResponse toResponse(Milestone m, BigDecimal currentPortfolioValueBase) {
        BigDecimal progress = BigDecimal.ZERO;
        if (m.getThresholdValueBase() != null && m.getThresholdValueBase().compareTo(BigDecimal.ZERO) > 0
                && currentPortfolioValueBase != null) {
            progress = currentPortfolioValueBase
                    .divide(m.getThresholdValueBase(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .min(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return MilestoneResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .thresholdValueBase(m.getThresholdValueBase())
                .comparisonLabel(m.getComparisonLabel())
                .achieved(m.isAchieved())
                .achievedDate(m.getAchievedDate())
                .progressPercent(progress)
                .build();
    }
}
