package com.portfoliomanager.service;

import com.portfoliomanager.dto.InvestmentRequest;
import com.portfoliomanager.dto.InvestmentResponse;
import com.portfoliomanager.dto.PriceUpdateRequest;
import com.portfoliomanager.exception.InvalidOperationException;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.repository.InvestmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private CurrencyService currencyService;

    private InvestmentService investmentService;

    @BeforeEach
    void setUp() {
        investmentService = new InvestmentService(investmentRepository, currencyService);
        lenient().when(currencyService.toBase(any(), anyString())).thenAnswer(invocation -> {
            BigDecimal amount = invocation.getArgument(0);
            return amount == null ? BigDecimal.ZERO : amount;
        });
    }

    @Test
    void create_stockCalculatesAmountsAndDefaultsCurrentPrice() {
        InvestmentRequest request = new InvestmentRequest();
        request.setType(InvestmentType.STOCK);
        request.setSymbol("AAPL");
        request.setName("Apple Inc.");
        request.setCountry("US");
        request.setCurrency("usd");
        request.setQuantity(new BigDecimal("10"));
        request.setAvgBuyPrice(new BigDecimal("100"));
        request.setCurrentPrice(null);
        request.setNotes("Long term");

        when(investmentRepository.save(any())).thenAnswer(invocation -> {
            Investment inv = invocation.getArgument(0);
            inv.setId(1L);
            return inv;
        });

        InvestmentResponse response = investmentService.create(request);

        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).save(captor.capture());
        Investment saved = captor.getValue();

        assertEquals(new BigDecimal("1000"), saved.getInvestedAmount());
        assertEquals(new BigDecimal("1000"), saved.getCurrentValue());
        assertEquals(new BigDecimal("100"), saved.getCurrentPrice());
        assertEquals(new BigDecimal("1000"), saved.getPreviousValue());
        assertEquals("USD", saved.getCurrency());
        assertEquals(InvestmentStatus.ACTIVE, saved.getStatus());

        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("0.00"), response.getUnrealizedPlPercent());
    }

    @Test
    void create_fdDefaultsCurrentValueToInvestedAmount() {
        InvestmentRequest request = new InvestmentRequest();
        request.setType(InvestmentType.FD);
        request.setSymbol("FD001");
        request.setName("Bank FD");
        request.setCountry("India");
        request.setCurrency("inr");
        request.setInvestedAmount(new BigDecimal("5000"));
        request.setCurrentValue(null);

        when(investmentRepository.save(any())).thenAnswer(invocation -> {
            Investment inv = invocation.getArgument(0);
            inv.setId(2L);
            return inv;
        });

        investmentService.create(request);

        ArgumentCaptor<Investment> captor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).save(captor.capture());
        Investment saved = captor.getValue();

        assertEquals(new BigDecimal("5000"), saved.getInvestedAmount());
        assertEquals(new BigDecimal("5000"), saved.getCurrentValue());
        assertEquals(new BigDecimal("5000"), saved.getPreviousValue());
        assertEquals("INR", saved.getCurrency());
    }

    @Test
    void create_stockWithoutQuantityThrowsInvalidOperation() {
        InvestmentRequest request = new InvestmentRequest();
        request.setType(InvestmentType.STOCK);
        request.setSymbol("AAPL");
        request.setName("Apple Inc.");
        request.setCountry("US");
        request.setCurrency("USD");
        request.setAvgBuyPrice(new BigDecimal("100"));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> investmentService.create(request));

        assertTrue(ex.getMessage().contains("quantity and avgBuyPrice"));
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void create_fdWithoutInvestedAmountThrowsInvalidOperation() {
        InvestmentRequest request = new InvestmentRequest();
        request.setType(InvestmentType.FD);
        request.setSymbol("FD001");
        request.setName("Bank FD");
        request.setCountry("India");
        request.setCurrency("INR");

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> investmentService.create(request));

        assertTrue(ex.getMessage().contains("investedAmount"));
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void findById_whenMissingThrowsResourceNotFound() {
        when(investmentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> investmentService.findById(42L));
    }

    @Test
    void findAll_mapsAllInvestmentsToResponses() {
        Investment stock = baseInvestment(1L, InvestmentType.STOCK);
        stock.setInvestedAmount(new BigDecimal("1000"));
        stock.setCurrentValue(new BigDecimal("1100"));
        stock.setCurrency("USD");

        when(investmentRepository.findAll(InvestmentType.STOCK, "US", InvestmentStatus.ACTIVE))
                .thenReturn(List.of(stock));

        List<InvestmentResponse> responses = investmentService.findAll(InvestmentType.STOCK, "US", InvestmentStatus.ACTIVE);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(0, new BigDecimal("100.00").compareTo(responses.get(0).getUnrealizedPl()));
        assertEquals(new BigDecimal("10.00"), responses.get(0).getUnrealizedPlPercent());
    }

    @Test
    void update_changesEditableFieldsAndPreservesPurchaseDateWhenMissing() {
        Investment existing = baseInvestment(1L, InvestmentType.ETF);
        existing.setQuantity(new BigDecimal("20"));
        existing.setAvgBuyPrice(new BigDecimal("50"));
        existing.setPurchaseDate(LocalDate.of(2024, 1, 1));

        InvestmentRequest request = new InvestmentRequest();
        request.setType(InvestmentType.ETF);
        request.setSymbol("QQQ");
        request.setName("Nasdaq ETF Updated");
        request.setCountry("US");
        request.setCurrency("usd");
        request.setQuantity(new BigDecimal("999"));
        request.setAvgBuyPrice(new BigDecimal("999"));
        request.setMaturityDate(LocalDate.of(2030, 12, 31));
        request.setInterestRate(new BigDecimal("7.5"));
        request.setNotes("updated notes");
        request.setPurchaseDate(null);

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(investmentRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InvestmentResponse response = investmentService.update(1L, request);

        assertEquals("Nasdaq ETF Updated", response.getName());
        assertEquals("USD", response.getCurrency());
        assertEquals(LocalDate.of(2024, 1, 1), response.getPurchaseDate());
        assertEquals(new BigDecimal("20"), response.getQuantity());
        assertEquals(new BigDecimal("50"), response.getAvgBuyPrice());
    }

    @Test
    void updatePrice_stockRequiresCurrentPrice() {
        Investment stock = baseInvestment(1L, InvestmentType.STOCK);
        when(investmentRepository.findById(1L)).thenReturn(Optional.of(stock));

        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setCurrentPrice(null);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> investmentService.updatePrice(1L, request));
        assertTrue(ex.getMessage().contains("currentPrice is required"));
        verify(investmentRepository, never()).updatePrice(anyLong(), any(), any());
    }

    @Test
    void updatePrice_stockComputesCurrentValueFromQuantity() {
        Investment stock = baseInvestment(1L, InvestmentType.STOCK);
        stock.setQuantity(new BigDecimal("10"));
        stock.setCurrentPrice(new BigDecimal("90"));

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(stock));

        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setCurrentPrice(new BigDecimal("120"));

        investmentService.updatePrice(1L, request);

        verify(investmentRepository).updatePrice(eq(1L), eq(new BigDecimal("120")), eq(new BigDecimal("1200")));
    }

    @Test
    void updatePrice_fdRequiresCurrentValue() {
        Investment fd = baseInvestment(5L, InvestmentType.FD);
        when(investmentRepository.findById(5L)).thenReturn(Optional.of(fd));

        PriceUpdateRequest request = new PriceUpdateRequest();

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> investmentService.updatePrice(5L, request));

        assertTrue(ex.getMessage().contains("currentValue is required"));
        verify(investmentRepository, never()).updatePrice(anyLong(), any(), any());
    }

    @Test
    void updatePrice_fdUsesProvidedCurrentValue() {
        Investment fd = baseInvestment(5L, InvestmentType.FD);
        fd.setCurrentPrice(new BigDecimal("1"));
        when(investmentRepository.findById(5L)).thenReturn(Optional.of(fd));

        PriceUpdateRequest request = new PriceUpdateRequest();
        request.setCurrentValue(new BigDecimal("6400"));

        investmentService.updatePrice(5L, request);

        verify(investmentRepository).updatePrice(eq(5L), eq(new BigDecimal("1")), eq(new BigDecimal("6400")));
    }

    @Test
    void delete_missingInvestmentThrowsResourceNotFound() {
        when(investmentRepository.existsById(11L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> investmentService.delete(11L));
        verify(investmentRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_existingInvestmentDeletesById() {
        when(investmentRepository.existsById(11L)).thenReturn(true);

        investmentService.delete(11L);

        verify(investmentRepository).deleteById(11L);
    }

    private Investment baseInvestment(Long id, InvestmentType type) {
        return Investment.builder()
                .id(id)
                .type(type)
                .symbol("SYM")
                .name("Sample")
                .country("US")
                .currency("USD")
                .quantity(new BigDecimal("10"))
                .avgBuyPrice(new BigDecimal("100"))
                .currentPrice(new BigDecimal("100"))
                .investedAmount(new BigDecimal("1000"))
                .currentValue(new BigDecimal("1000"))
                .previousValue(new BigDecimal("1000"))
                .status(InvestmentStatus.ACTIVE)
                .build();
    }
}
