package com.portfoliomanager.controller;

import com.portfoliomanager.dto.ExchangeRateRequest;
import com.portfoliomanager.model.ExchangeRate;
import com.portfoliomanager.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateControllerTest {

    @Mock
    private CurrencyService currencyService;

    private ExchangeRateController controller;

    @BeforeEach
    void setUp() {
        controller = new ExchangeRateController(currencyService);
    }

    @Test
    void getBaseCurrency_returnsMapFromServiceValue() {
        when(currencyService.getBaseCurrency()).thenReturn("INR");

        Map<String, String> result = controller.getBaseCurrency();

        assertEquals("INR", result.get("baseCurrency"));
        verify(currencyService).getBaseCurrency();
    }

    @Test
    void setBaseCurrency_validInputSetsAndReturnsCurrentBase() {
        when(currencyService.getBaseCurrency()).thenReturn("USD");

        Map<String, String> result = controller.setBaseCurrency(Map.of("baseCurrency", "usd"));

        verify(currencyService).setBaseCurrency("usd");
        verify(currencyService).getBaseCurrency();
        assertEquals("USD", result.get("baseCurrency"));
    }

    @Test
    void setBaseCurrency_missingOrBlankThrowsIllegalArgumentException() {
        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class,
                () -> controller.setBaseCurrency(Map.of()));
        assertTrue(missing.getMessage().contains("baseCurrency is required"));

        IllegalArgumentException blank = assertThrows(IllegalArgumentException.class,
                () -> controller.setBaseCurrency(Map.of("baseCurrency", "   ")));
        assertTrue(blank.getMessage().contains("baseCurrency is required"));

        verify(currencyService, never()).setBaseCurrency("   ");
    }

    @Test
    void findAll_returnsRatesFromService() {
        ExchangeRate usd = ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("83.1000")).build();
        when(currencyService.getAllRates()).thenReturn(List.of(usd));

        List<ExchangeRate> result = controller.findAll();

        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getCurrencyCode());
        verify(currencyService).getAllRates();
    }

    @Test
    void upsert_delegatesToService() {
        ExchangeRateRequest request = new ExchangeRateRequest();
        request.setRateToBase(new BigDecimal("84.2000"));
        ExchangeRate updated = ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("84.2000")).build();
        when(currencyService.upsertRate("usd", request)).thenReturn(updated);

        ExchangeRate result = controller.upsert("usd", request);

        assertEquals("USD", result.getCurrencyCode());
        assertEquals(0, new BigDecimal("84.2000").compareTo(result.getRateToBase()));
        verify(currencyService).upsertRate("usd", request);
    }
}
