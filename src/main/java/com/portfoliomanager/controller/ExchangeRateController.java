package com.portfoliomanager.controller;

import com.portfoliomanager.dto.ExchangeRateRequest;
import com.portfoliomanager.model.ExchangeRate;
import com.portfoliomanager.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Manages currency conversion so the customer can view values in both the original
 * currency and their preferred base currency, across investments in India, US, UK,
 * Europe and China.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Currency", description = "Base currency setting and currency conversion rates")
public class ExchangeRateController {

    private final CurrencyService currencyService;

    public ExchangeRateController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Operation(summary = "Get the customer's base/preferred currency")
    @GetMapping("/settings/base-currency")
    public Map<String, String> getBaseCurrency() {
        return Map.of("baseCurrency", currencyService.getBaseCurrency());
    }

    @Operation(summary = "Set the customer's base/preferred currency")
    @PutMapping("/settings/base-currency")
    public Map<String, String> setBaseCurrency(@RequestBody Map<String, String> body) {
        String currency = body.get("baseCurrency");
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("baseCurrency is required");
        }
        currencyService.setBaseCurrency(currency);
        return Map.of("baseCurrency", currencyService.getBaseCurrency());
    }

    @Operation(summary = "List all configured exchange rates (rate to base currency)")
    @GetMapping("/exchange-rates")
    public List<ExchangeRate> findAll() {
        return currencyService.getAllRates();
    }

    @Operation(summary = "Create or update the exchange rate for a currency")
    @PutMapping("/exchange-rates/{currencyCode}")
    public ExchangeRate upsert(@PathVariable String currencyCode, @Valid @RequestBody ExchangeRateRequest request) {
        return currencyService.upsertRate(currencyCode, request);
    }
}
