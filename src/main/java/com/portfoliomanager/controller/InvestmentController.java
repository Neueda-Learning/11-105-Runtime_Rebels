package com.portfoliomanager.controller;

import com.portfoliomanager.dto.InvestmentRequest;
import com.portfoliomanager.dto.InvestmentResponse;
import com.portfoliomanager.dto.PriceUpdateRequest;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD for investments (stocks, ETFs, fixed deposits, cash) - lets the customer browse
 * and manage their portfolio from one place, across investment types.
 */
@RestController
@RequestMapping("/api/investments")
@Tag(name = "Investments", description = "Browse, add, update and remove investments (stocks, ETFs, FDs, cash)")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @Operation(summary = "List investments, optionally filtered by type, country and status")
    @GetMapping
    public List<InvestmentResponse> findAll(
            @RequestParam(required = false) InvestmentType type,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) InvestmentStatus status) {
        return investmentService.findAll(type, country, status);
    }

    @Operation(summary = "Get a single investment by id")
    @GetMapping("/{id}")
    public InvestmentResponse findById(@PathVariable Long id) {
        return investmentService.findById(id);
    }

    @Operation(summary = "Add a new investment to the portfolio")
    @PostMapping
    public ResponseEntity<InvestmentResponse> create(@Valid @RequestBody InvestmentRequest request) {
        InvestmentResponse created = investmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update descriptive details of an investment (not quantity/value - use transactions for that)")
    @PutMapping("/{id}")
    public InvestmentResponse update(@PathVariable Long id, @Valid @RequestBody InvestmentRequest request) {
        return investmentService.update(id, request);
    }

    @Operation(summary = "Refresh the market price/value of an investment")
    @PatchMapping("/{id}/price")
    public InvestmentResponse updatePrice(@PathVariable Long id, @Valid @RequestBody PriceUpdateRequest request) {
        return investmentService.updatePrice(id, request);
    }

    @Operation(summary = "Remove an investment from the portfolio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        investmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
