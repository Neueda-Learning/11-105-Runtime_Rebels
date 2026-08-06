package com.portfoliomanager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Locale;

/** Types of investments the customer wants to track from a single place. */
public enum InvestmentType {
    CASH,
    STOCK,
    ETF,
    FD,
    COMMODITY

    ;

    @JsonCreator
    public static InvestmentType from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Investment type is required. Allowed values: " + Arrays.toString(values()));
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if ("COMMODITIES".equals(normalized)) {
            normalized = "COMMODITY";
        }

        try {
            return InvestmentType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid investment type '" + raw + "'. Allowed values: " + Arrays.toString(values())
            );
        }
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
