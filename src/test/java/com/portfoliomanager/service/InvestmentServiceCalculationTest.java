package com.portfoliomanager.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvestmentServiceCalculationTest {

    @Test
    void percentChange_computesPositiveReturn() {
        BigDecimal invested = new BigDecimal("1000");
        BigDecimal gain = new BigDecimal("150");

        BigDecimal percent = InvestmentService.percentChange(invested, gain);

        assertEquals(new BigDecimal("15.00"), percent);
    }

    @Test
    void percentChange_handlesZeroBaseWithoutError() {
        BigDecimal percent = InvestmentService.percentChange(BigDecimal.ZERO, new BigDecimal("50"));
        assertEquals(BigDecimal.ZERO, percent);
    }

    @Test
    void percentChange_computesNegativeReturn() {
        BigDecimal invested = new BigDecimal("2000");
        BigDecimal loss = new BigDecimal("-100");

        BigDecimal percent = InvestmentService.percentChange(invested, loss);

        assertEquals(new BigDecimal("-5.00"), percent);
    }
}
