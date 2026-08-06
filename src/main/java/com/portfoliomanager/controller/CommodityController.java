package com.portfoliomanager.controller;

import com.portfoliomanager.dto.CommodityRequest;
import com.portfoliomanager.dto.CommodityResponse;
import com.portfoliomanager.service.CommodityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commodities")
@Tag(name = "Commodities", description = "CRUD operations for commodity investments")
public class CommodityController {

    private final CommodityService commodityService;

    public CommodityController(CommodityService commodityService) {
        this.commodityService = commodityService;
    }

    @Operation(summary = "Add a new commodity")
    @PostMapping
    public ResponseEntity<CommodityResponse> create(@Valid @RequestBody CommodityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commodityService.create(request));
    }

    @Operation(summary = "List all commodities")
    @GetMapping
    public List<CommodityResponse> findAll() {
        return commodityService.findAll();
    }

    @Operation(summary = "Get commodity by id")
    @GetMapping("/{id}")
    public CommodityResponse findById(@PathVariable Long id) {
        return commodityService.findById(id);
    }

    @Operation(summary = "Update commodity")
    @PutMapping("/{id}")
    public CommodityResponse update(@PathVariable Long id, @Valid @RequestBody CommodityRequest request) {
        return commodityService.update(id, request);
    }

    @Operation(summary = "Delete commodity")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commodityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

