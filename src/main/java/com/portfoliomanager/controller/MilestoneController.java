package com.portfoliomanager.controller;

import com.portfoliomanager.dto.DashboardResponse;
import com.portfoliomanager.dto.MilestoneRequest;
import com.portfoliomanager.dto.MilestoneResponse;
import com.portfoliomanager.service.DashboardService;
import com.portfoliomanager.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Wealth-related, feel-good milestones - e.g. "portfolio crossed the price of a luxury car" -
 * a fun/engaging feature the customer specifically requested.
 */
@RestController
@RequestMapping("/api/milestones")
@Tag(name = "Milestones", description = "Wealth milestones that make portfolio tracking more engaging")
public class MilestoneController {

    private final MilestoneService milestoneService;
    private final DashboardService dashboardService;

    public MilestoneController(MilestoneService milestoneService, DashboardService dashboardService) {
        this.milestoneService = milestoneService;
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "List all milestones with progress toward each, based on current portfolio value")
    @GetMapping
    public List<MilestoneResponse> findAll() {
        DashboardResponse dashboard = dashboardService.getDashboard();
        return milestoneService.findAll(dashboard.getTotalCurrentValue());
    }

    @Operation(summary = "Create a custom milestone (e.g. compare against a specific aspirational purchase)")
    @PostMapping
    public ResponseEntity<MilestoneResponse> create(@Valid @RequestBody MilestoneRequest request) {
        MilestoneResponse created = milestoneService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Delete a milestone")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        milestoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
