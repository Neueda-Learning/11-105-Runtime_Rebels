package com.portfoliomanager.service;

import com.portfoliomanager.dto.ExchangeRateRequest;
import com.portfoliomanager.model.ExchangeRate;
import com.portfoliomanager.repository.ExchangeRateRepository;
import com.portfoliomanager.repository.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private SettingRepository settingRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
    }

    @Test
    void getBaseCurrency_returnsDefaultWhenNotConfigured() {
        when(settingRepository.get(USER_ID, "base_currency")).thenReturn(Optional.empty());

        String base = currencyService.getBaseCurrency();

        assertEquals("INR", base);
    }

    @Test
    void setBaseCurrency_returnsEarlyWhenCurrencyIsSame() {
        when(settingRepository.get(USER_ID, "base_currency")).thenReturn(Optional.of("INR"));

        currencyService.setBaseCurrency("inr");

        verify(exchangeRateRepository, never()).findByCurrencyCode(anyLong(), anyString());
        verify(exchangeRateRepository, never()).findAll(anyLong());
        verify(exchangeRateRepository, never()).upsert(anyLong(), anyString(), any(BigDecimal.class));
        verify(settingRepository, never()).set(anyLong(), anyString(), anyString());
    }

    @Test
    void setBaseCurrency_throwsWhenNewBaseRateIsZero() {
        when(settingRepository.get(USER_ID, "base_currency")).thenReturn(Optional.of("INR"));
        when(exchangeRateRepository.findByCurrencyCode(USER_ID, "USD"))
                .thenReturn(
                        Optional.of(ExchangeRate.builder().currencyCode("USD").rateToBase(BigDecimal.ZERO).build()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> currencyService.setBaseCurrency("usd"));

        assertEquals("Exchange rate for new base currency cannot be zero.", ex.getMessage());
        verify(settingRepository, never()).set(eq(USER_ID), eq("base_currency"), eq("USD"));
    }

    @Test
    void setBaseCurrency_recalculatesRatesAndUpdatesBaseSetting() {
        when(settingRepository.get(USER_ID, "base_currency")).thenReturn(Optional.of("INR"));
        when(exchangeRateRepository.findByCurrencyCode(USER_ID, "USD"))
                .thenReturn(Optional.of(
                        ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("80.000000")).build()));
        when(exchangeRateRepository.findAll(USER_ID)).thenReturn(List.of(
                ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("80.000000")).build(),
                ExchangeRate.builder().currencyCode("EUR").rateToBase(new BigDecimal("90.000000")).build(),
                ExchangeRate.builder().currencyCode("INR").rateToBase(BigDecimal.ONE).build()));

        currencyService.setBaseCurrency("usd");

        verify(exchangeRateRepository).upsert(USER_ID, "INR", new BigDecimal("0.012500"));
        verify(exchangeRateRepository).upsert(USER_ID, "USD", BigDecimal.ONE);
        verify(exchangeRateRepository).upsert(USER_ID, "EUR", new BigDecimal("1.125000"));
        verify(settingRepository).set(USER_ID, "base_currency", "USD");
    }

    @Test
    void upsertRate_uppercasesCurrencyCode() {
        ExchangeRateRequest request = new ExchangeRateRequest();
        request.setRateToBase(new BigDecimal("1.2345"));

        ExchangeRate expected = ExchangeRate.builder()
                .currencyCode("EUR")
                .rateToBase(new BigDecimal("1.2345"))
                .build();

        when(exchangeRateRepository.upsert(USER_ID, "EUR", new BigDecimal("1.2345"))).thenReturn(expected);

        ExchangeRate result = currencyService.upsertRate("eur", request);

        assertEquals("EUR", result.getCurrencyCode());
        assertEquals(new BigDecimal("1.2345"), result.getRateToBase());
    }

    @Test
    void toBase_returnsZeroWhenAmountIsNull() {
        BigDecimal converted = currencyService.toBase(null, "USD");

        assertEquals(BigDecimal.ZERO, converted);
        verify(exchangeRateRepository, never()).findByCurrencyCode(anyLong(), anyString());
    }

    @Test
    void toBase_usesRateAndRoundsToFourDecimals() {
        when(exchangeRateRepository.findByCurrencyCode(USER_ID, "USD"))
                .thenReturn(Optional.of(
                        ExchangeRate.builder().currencyCode("USD").rateToBase(new BigDecimal("83.123456")).build()));

        BigDecimal converted = currencyService.toBase(new BigDecimal("2.5"), "USD");

        assertEquals(new BigDecimal("207.8086"), converted);
    }

    @Test
    void toBase_defaultsToOneWhenRateMissing() {
        when(exchangeRateRepository.findByCurrencyCode(USER_ID, "ABC")).thenReturn(Optional.empty());

        BigDecimal converted = currencyService.toBase(new BigDecimal("12.34567"), "ABC");

        assertEquals(new BigDecimal("12.3457"), converted);
    }
}
