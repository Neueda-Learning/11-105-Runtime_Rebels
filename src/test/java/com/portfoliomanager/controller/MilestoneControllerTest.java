package com.portfoliomanager.controller;

import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.dto.MilestoneRequest;
import com.portfoliomanager.dto.MilestoneResponse;
import com.portfoliomanager.service.DashboardService;
import com.portfoliomanager.service.MilestoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneControllerTest {

    @Mock
    private MilestoneService milestoneService;

    @Mock
    private DashboardService dashboardService;

    private MilestoneController controller;

    @BeforeEach
    void setUp() {
        controller = new MilestoneController(milestoneService, dashboardService);
    }

    @Test
    void findAll_usesDashboardCurrentValueForProgress() {
        DashboardResponse dashboard = DashboardResponse.builder()
                .totalCurrentValue(new BigDecimal("15000"))
                .build();
        MilestoneResponse milestone = MilestoneResponse.builder().id(1L).build();

        when(dashboardService.getDashboard()).thenReturn(dashboard);
        when(milestoneService.findAll(new BigDecimal("15000"))).thenReturn(List.of(milestone));

        List<MilestoneResponse> result = controller.findAll();

        assertEquals(1, result.size());
        assertSame(milestone, result.get(0));
        verify(dashboardService).getDashboard();
        verify(milestoneService).findAll(new BigDecimal("15000"));
    }

    @Test
    void create_returnsCreatedResponseEntity() {
        MilestoneRequest request = new MilestoneRequest();
        MilestoneResponse created = MilestoneResponse.builder().id(2L).build();
        when(milestoneService.create(request)).thenReturn(created);

        ResponseEntity<MilestoneResponse> result = controller.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(created, result.getBody());
        verify(milestoneService).create(request);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> result = controller.delete(5L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(milestoneService).delete(5L);
    }
}
