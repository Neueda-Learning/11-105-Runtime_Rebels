package com.portfoliomanager.service;

import com.portfoliomanager.dto.ExchangeRateRequest;
import com.portfoliomanager.model.ExchangeRate;
import com.portfoliomanager.repository.ExchangeRateRepository;
import com.portfoliomanager.repository.SettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private SettingRepository settingRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void getBaseCurrency_returnsDefaultWhenNotConfigured() {
        when(settingRepository.get("base_currency")).thenReturn(Optional.empty());

        String base = currencyService.getBaseCurrency();

        assertEquals("INR", base);
    }

    @Test
    void setBaseCurrency_returnsEarlyWhenCurrencyIsSame() {
        when(settingRepository.get("base_currency")).thenReturn(Optional.of("INR"));

        currencyService.setBaseCurrency("inr");

        verify(exchangeRateRepository, never()).findByCurrencyCode(anyString());
        verify(exchangeRateRepository, never()).findAll();
        verify(exchangeRateRepository, never()).upsert(anyString(), any(BigDecimal.class));
        verify(settingRepository, never()).set(anyString(), anyString());
    }

    @Test
    void setBaseCurrency_throwsWhenNewBaseRateIsZero() {
        when(settingRepository.get("base_currency")).thenReturn(Optional.of("INR"));
        when(exchangeRateRepository.findByCurrencyCode("USD"))
                .thenReturn(
                        Optional.of(ExchangeRate.builder().currencyCode("USD").rateToBase(BigDecimal.ZERO).build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> currencyService.setBaseCurrency("usd"));

        assertEquals("Exchange rate for new base currency cannot be zero.", ex.getMessage());
        verify(settingRepository, never()).set("base_currency", "USD");
    }

    @Test
    void setBaseCurrency_recalculatesRatesAndUpdatesBaseSetting() {
        when(settingRepository.get("base_currency")).thenReturn(Optional.of("INR"));
        when(exchangeRateRepository.findByCurrencyCode("USD"))
                .thenReturn(Optional.of(
                        ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("80.000000")).build()));
        when(exchangeRateRepository.findAll()).thenReturn(List.of(
                ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("80.000000")).build(),
                ExchangeRate.builder().currencyCode("EUR").rateToBase(new BigDecimal("90.000000")).build(),
                ExchangeRate.builder().currencyCode("INR").rateToBase(BigDecimal.ONE).build()));

        currencyService.setBaseCurrency("usd");

        verify(exchangeRateRepository).upsert("INR", new BigDecimal("0.012500"));
        verify(exchangeRateRepository).upsert("USD", BigDecimal.ONE);
        verify(exchangeRateRepository).upsert("EUR", new BigDecimal("1.125000"));
        verify(settingRepository).set("base_currency", "USD");
    }

    @Test
    void upsertRate_uppercasesCurrencyCode() {
        ExchangeRateRequest request = new ExchangeRateRequest();
        request.setRateToBase(new BigDecimal("1.2345"));

        ExchangeRate expected = ExchangeRate.builder()
                .currencyCode("EUR")
                .rateToBase(new BigDecimal("1.2345"))
                .build();

        when(exchangeRateRepository.upsert("EUR", new BigDecimal("1.2345"))).thenReturn(expected);

        ExchangeRate result = currencyService.upsertRate("eur", request);

        assertEquals("EUR", result.getCurrencyCode());
        assertEquals(new BigDecimal("1.2345"), result.getRateToBase());
    }

    @Test
    void toBase_returnsZeroWhenAmountIsNull() {
        BigDecimal converted = currencyService.toBase(null, "USD");

        assertEquals(BigDecimal.ZERO, converted);
        verify(exchangeRateRepository, never()).findByCurrencyCode(anyString());
    }

    @Test
    void toBase_usesRateAndRoundsToFourDecimals() {
        when(exchangeRateRepository.findByCurrencyCode("USD"))
                .thenReturn(Optional.of(
                        ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("83.123456")).build()));

        BigDecimal converted = currencyService.toBase(new BigDecimal("2.5"), "USD");

        assertEquals(new BigDecimal("207.8086"), converted);
    }

    @Test
    void toBase_defaultsToOneWhenRateMissing() {
        when(exchangeRateRepository.findByCurrencyCode("ABC")).thenReturn(Optional.empty());

        BigDecimal converted = currencyService.toBase(new BigDecimal("12.34567"), "ABC");

        assertEquals(new BigDecimal("12.3457"), converted);
    }
}
