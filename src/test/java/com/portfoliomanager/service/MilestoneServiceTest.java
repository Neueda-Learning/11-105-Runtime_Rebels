package com.portfoliomanager.service;

import com.portfoliomanager.dto.MilestoneRequest;
import com.portfoliomanager.dto.MilestoneResponse;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Milestone;
import com.portfoliomanager.repository.MilestoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private CurrentUserService currentUserService;

    private MilestoneService milestoneService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        milestoneService = new MilestoneService(milestoneRepository, currentUserService);
    }

    @Test
    void create_setsDefaultsAndReturnsResponse() {
        MilestoneRequest request = new MilestoneRequest();
        request.setName("Luxury Car");
        request.setThresholdValueBase(new BigDecimal("5000000"));
        request.setComparisonLabel("Porsche 911");

        when(milestoneRepository.save(eq(USER_ID), any())).thenAnswer(invocation -> {
            Milestone m = invocation.getArgument(1);
            m.setId(1L);
            return m;
        });

        MilestoneResponse response = milestoneService.create(request);

        ArgumentCaptor<Milestone> captor = ArgumentCaptor.forClass(Milestone.class);
        verify(milestoneRepository).save(eq(USER_ID), captor.capture());
        Milestone saved = captor.getValue();

        assertFalse(saved.isAchieved());
        assertEquals("Luxury Car", saved.getName());
        assertEquals(new BigDecimal("5000000"), saved.getThresholdValueBase());

        assertEquals(1L, response.getId());
        assertEquals(0, new BigDecimal("0").compareTo(response.getProgressPercent()));
        assertFalse(response.isAchieved());
    }

    @Test
    void findAll_mapsProgressAndCapsAtHundred() {
        Milestone first = milestone(1L, "Bike", "1000", false, null);
        Milestone second = milestone(2L, "Car", "10000", true, LocalDate.of(2025, 1, 1));
        when(milestoneRepository.findAll(USER_ID)).thenReturn(List.of(first, second));

        List<MilestoneResponse> responses = milestoneService.findAll(new BigDecimal("2500"));

        assertEquals(2, responses.size());
        assertEquals(new BigDecimal("100.00"), responses.get(0).getProgressPercent());
        assertEquals(new BigDecimal("25.00"), responses.get(1).getProgressPercent());
    }

    @Test
    void findAll_whenCurrentValueNullProgressIsZero() {
        Milestone first = milestone(1L, "Bike", "1000", false, null);
        when(milestoneRepository.findAll(USER_ID)).thenReturn(List.of(first));

        List<MilestoneResponse> responses = milestoneService.findAll(null);

        assertEquals(new BigDecimal("0"), responses.get(0).getProgressPercent());
    }

    @Test
    void delete_whenRepositoryReturnsFalseThrowsNotFound() {
        when(milestoneRepository.deleteById(USER_ID, 99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> milestoneService.delete(99L));
    }

    @Test
    void delete_whenRepositoryReturnsTrueSucceeds() {
        when(milestoneRepository.deleteById(USER_ID, 99L)).thenReturn(true);

        milestoneService.delete(99L);

        verify(milestoneRepository).deleteById(USER_ID, 99L);
    }

    @Test
    void refreshAchievedStatus_marksOnlyEligibleUnachievedMilestones() {
        Milestone achievedAlready = milestone(1L, "Small", "1000", true, LocalDate.of(2024, 1, 1));
        Milestone shouldMark = milestone(2L, "Medium", "2000", false, null);
        Milestone shouldNotMark = milestone(3L, "Large", "5000", false, null);

        when(milestoneRepository.findAll(USER_ID)).thenReturn(List.of(achievedAlready, shouldMark, shouldNotMark));

        milestoneService.refreshAchievedStatus(new BigDecimal("3000"));

        verify(milestoneRepository).markAchieved(eq(USER_ID), eq(2L), any(LocalDate.class));
        verify(milestoneRepository, never()).markAchieved(eq(USER_ID), eq(1L), any(LocalDate.class));
        verify(milestoneRepository, never()).markAchieved(eq(USER_ID), eq(3L), any(LocalDate.class));
    }

    @Test
    void findNext_returnsLowestThresholdAmongUnachieved() {
        Milestone m1 = milestone(1L, "A", "10000", false, null);
        Milestone m2 = milestone(2L, "B", "5000", false, null);
        Milestone m3 = milestone(3L, "C", "3000", true, LocalDate.of(2025, 2, 2));
        when(milestoneRepository.findAll(USER_ID)).thenReturn(List.of(m1, m2, m3));

        Optional<MilestoneResponse> next = milestoneService.findNext(new BigDecimal("2500"));

        assertTrue(next.isPresent());
        assertEquals(2L, next.get().getId());
        assertEquals(new BigDecimal("50.00"), next.get().getProgressPercent());
    }

    @Test
    void findNext_returnsEmptyWhenAllAreAchieved() {
        Milestone m1 = milestone(1L, "A", "1000", true, LocalDate.of(2025, 1, 1));
        when(milestoneRepository.findAll(USER_ID)).thenReturn(List.of(m1));

        Optional<MilestoneResponse> next = milestoneService.findNext(new BigDecimal("5000"));

        assertTrue(next.isEmpty());
    }

    @Test
    void countAchieved_countsOnlyTrueFlags() {
        Milestone m1 = milestone(1L, "A", "1000", true, LocalDate.of(2025, 1, 1));
        Milestone m2 = milestone(2L, "B", "2000", false, null);
        Milestone m3 = milestone(3L, "C", "3000", true, LocalDate.of(2025, 1, 1));
        when(milestoneRepository.findAll(USER_ID)).thenReturn(List.of(m1, m2, m3));

        long count = milestoneService.countAchieved();

        assertEquals(2, count);
    }

    private Milestone milestone(Long id, String name, String threshold, boolean achieved, LocalDate achievedDate) {
        return Milestone.builder()
                .id(id)
                .name(name)
                .thresholdValueBase(new BigDecimal(threshold))
                .comparisonLabel(name + " label")
                .achieved(achieved)
                .achievedDate(achievedDate)
                .build();
    }
}
