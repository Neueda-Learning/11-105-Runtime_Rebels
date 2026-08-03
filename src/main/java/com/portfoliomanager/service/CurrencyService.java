package com.portfoliomanager.service;

import com.portfoliomanager.dto.ExchangeRateRequest;
import com.portfoliomanager.model.ExchangeRate;
import com.portfoliomanager.repository.ExchangeRateRepository;
import com.portfoliomanager.repository.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CurrencyService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final SettingRepository settingRepository;

    public CurrencyService(ExchangeRateRepository exchangeRateRepository, SettingRepository settingRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.settingRepository = settingRepository;
    }

    public String getBaseCurrency() {
        return settingRepository.get("base_currency").orElse("INR");
    }

    @Transactional
    public void setBaseCurrency(String newBaseCurrency) {
        String newBase = newBaseCurrency.toUpperCase();
        String oldBase = getBaseCurrency();

        // 1. Return early if the base currency is not changing
        if (oldBase.equalsIgnoreCase(newBase)) {
            return;
        }

        // 2. Fetch the current exchange rate of the new base currency
        BigDecimal rateOfNewBase = exchangeRateRepository.findByCurrencyCode(newBase)
                .map(ExchangeRate::getRateToBase)
                .orElse(BigDecimal.ONE);

        if (rateOfNewBase.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Exchange rate for new base currency cannot be zero.");
        }

        // 3. Save/Update the OLD base currency in exchange_rates table
        // (1 unit of oldBase = 1 / rateOfNewBase units of newBase)
        BigDecimal oldBaseNewRate = BigDecimal.ONE.divide(rateOfNewBase, 6, RoundingMode.HALF_UP);
        exchangeRateRepository.upsert(oldBase, oldBaseNewRate);

        // 4. Recalculate all other exchange rates w.r.t the new base currency
        List<ExchangeRate> existingRates = exchangeRateRepository.findAll();
        for (ExchangeRate rate : existingRates) {
            String code = rate.getCurrencyCode();

            if (code.equalsIgnoreCase(oldBase)) {
                continue; // Already saved in step 3
            }

            if (code.equalsIgnoreCase(newBase)) {
                exchangeRateRepository.upsert(newBase, BigDecimal.ONE);
            } else {
                BigDecimal newRate = rate.getRateToBase().divide(rateOfNewBase, 6, RoundingMode.HALF_UP);
                exchangeRateRepository.upsert(code, newRate);
            }
        }

        // 5. Update the base currency setting in the database
        settingRepository.set("base_currency", newBase);
    }

    public List<ExchangeRate> getAllRates() {
        return exchangeRateRepository.findAll();
    }

    public ExchangeRate upsertRate(String currencyCode, ExchangeRateRequest request) {
        return exchangeRateRepository.upsert(currencyCode.toUpperCase(), request.getRateToBase());
    }

    public BigDecimal toBase(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = exchangeRateRepository.findByCurrencyCode(currencyCode)
                .map(ExchangeRate::getRateToBase)
                .orElse(BigDecimal.ONE);
        return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
    }
}