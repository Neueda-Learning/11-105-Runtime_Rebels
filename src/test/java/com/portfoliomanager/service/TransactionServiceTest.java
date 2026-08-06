package com.portfoliomanager.service;

import com.portfoliomanager.dto.TransactionRequest;
import com.portfoliomanager.dto.TransactionResponse;
import com.portfoliomanager.exception.InvalidOperationException;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.TransactionRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestmentService investmentService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, investmentRepository, investmentService);
    }

    @Test
    void findByInvestment_mapsTransactionsWithInvestmentSymbol() {
        Investment inv = stockInvestment(10L, "AAPL", "USD", "5", "100", "500", "500");
        Transaction tx = tx(1L, 10L, TransactionType.BUY, "2", "120", "240", null);

        when(investmentService.getOrThrow(10L)).thenReturn(inv);
        when(transactionRepository.findByInvestmentId(10L)).thenReturn(List.of(tx));

        List<TransactionResponse> responses = transactionService.findByInvestment(10L);

        assertEquals(1, responses.size());
        assertEquals("AAPL", responses.get(0).getInvestmentSymbol());
        assertEquals(TransactionType.BUY, responses.get(0).getType());
    }

    @Test
    void findAll_resolvesSymbolIfInvestmentExistsElseNull() {
        Transaction tx1 = tx(1L, 10L, TransactionType.BUY, "2", "120", "240", null);
        Transaction tx2 = tx(2L, 11L, TransactionType.SELL, "1", "130", "130", "10");

        when(transactionRepository.findAll()).thenReturn(List.of(tx1, tx2));
        when(investmentRepository.findById(10L)).thenReturn(Optional.of(stockInvestment(10L, "AAPL", "USD", "1", "100", "100", "100")));
        when(investmentRepository.findById(11L)).thenReturn(Optional.empty());

        List<TransactionResponse> responses = transactionService.findAll();

        assertEquals(2, responses.size());
        assertEquals("AAPL", responses.get(0).getInvestmentSymbol());
        assertNull(responses.get(1).getInvestmentSymbol());
    }

    @Test
    void record_buyUpdatesPositionAndStoresTransaction() {
        Investment inv = stockInvestment(10L, "AAPL", "USD", "10", "100", "1000", "1000");
        when(investmentService.getOrThrow(10L)).thenReturn(inv);
        when(transactionRepository.save(any())).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(investmentRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.BUY);
        req.setQuantity(new BigDecimal("5"));
        req.setPrice(new BigDecimal("120"));
        req.setAmount(new BigDecimal("600"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));
        req.setNotes("buy more");

        TransactionResponse response = transactionService.record(10L, req);

        ArgumentCaptor<Investment> invCaptor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).update(invCaptor.capture());
        Investment updated = invCaptor.getValue();

        assertEquals(0, new BigDecimal("15").compareTo(updated.getQuantity()));
        assertEquals(0, new BigDecimal("106.666667").compareTo(updated.getAvgBuyPrice()));
        assertEquals(0, new BigDecimal("1600.000005").compareTo(updated.getInvestedAmount()));
        assertEquals(0, new BigDecimal("120").compareTo(updated.getCurrentPrice()));
        assertEquals(0, new BigDecimal("1800").compareTo(updated.getCurrentValue()));
        assertEquals(InvestmentStatus.ACTIVE, updated.getStatus());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertEquals(TransactionType.BUY, txCaptor.getValue().getType());
        assertEquals("USD", txCaptor.getValue().getCurrency());
        assertEquals("AAPL", response.getInvestmentSymbol());
    }

    @Test
    void record_buyForCommodityUpdatesPosition() {
        Investment commodity = stockInvestment(40L, "GOLD", "INR", "3", "100", "300", "300");
        commodity.setType(InvestmentType.COMMODITY);
        when(investmentService.getOrThrow(40L)).thenReturn(commodity);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.BUY);
        req.setQuantity(new BigDecimal("2"));
        req.setPrice(new BigDecimal("120"));
        req.setAmount(new BigDecimal("240"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        transactionService.record(40L, req);

        assertEquals(0, new BigDecimal("5").compareTo(commodity.getQuantity()));
        assertEquals(0, new BigDecimal("108.000000").compareTo(commodity.getAvgBuyPrice()));
        assertEquals(0, new BigDecimal("540.000000").compareTo(commodity.getInvestedAmount()));
        assertEquals(0, new BigDecimal("600").compareTo(commodity.getCurrentValue()));
    }

    @Test
    void record_sellComputesRealizedPlAndClosesWhenQuantityZero() {
        Investment inv = stockInvestment(10L, "AAPL", "USD", "5", "100", "500", "500");
        when(investmentService.getOrThrow(10L)).thenReturn(inv);
        when(transactionRepository.save(any())).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });
        when(investmentRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.SELL);
        req.setQuantity(new BigDecimal("5"));
        req.setPrice(new BigDecimal("110"));
        req.setAmount(new BigDecimal("550"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        TransactionResponse response = transactionService.record(10L, req);

        ArgumentCaptor<Investment> invCaptor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository).update(invCaptor.capture());
        Investment updated = invCaptor.getValue();
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getInvestedAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getCurrentValue()));
        assertEquals(InvestmentStatus.CLOSED, updated.getStatus());

        assertEquals(0, new BigDecimal("50.0000").compareTo(response.getRealizedPl()));
    }

    @Test
    void record_sellMoreThanHeldThrowsInvalidOperation() {
        Investment inv = stockInvestment(10L, "AAPL", "USD", "2", "100", "200", "200");
        when(investmentService.getOrThrow(10L)).thenReturn(inv);

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.SELL);
        req.setQuantity(new BigDecimal("3"));
        req.setPrice(new BigDecimal("120"));
        req.setAmount(new BigDecimal("360"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> transactionService.record(10L, req));

        assertTrue(ex.getMessage().contains("Cannot sell"));
        verify(investmentRepository, never()).update(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void record_depositForCashIncreasesInvestedAndCurrent() {
        Investment cash = cashInvestment(20L, "CASH", "INR", "1000", "1000");
        when(investmentService.getOrThrow(20L)).thenReturn(cash);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.DEPOSIT);
        req.setAmount(new BigDecimal("500"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        transactionService.record(20L, req);

        assertEquals(0, new BigDecimal("1500").compareTo(cash.getInvestedAmount()));
        assertEquals(0, new BigDecimal("1500").compareTo(cash.getCurrentValue()));
    }

    @Test
    void record_withdrawMoreThanBalanceThrowsInvalidOperation() {
        Investment cash = cashInvestment(20L, "CASH", "INR", "1000", "1000");
        when(investmentService.getOrThrow(20L)).thenReturn(cash);

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.WITHDRAW);
        req.setAmount(new BigDecimal("1200"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        assertThrows(InvalidOperationException.class, () -> transactionService.record(20L, req));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void record_withdrawForCashReducesBalanceAndFloorsInvestedAtZero() {
        Investment cash = cashInvestment(20L, "CASH", "INR", "100", "300");
        when(investmentService.getOrThrow(20L)).thenReturn(cash);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.WITHDRAW);
        req.setAmount(new BigDecimal("250"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        transactionService.record(20L, req);

        assertEquals(0, BigDecimal.ZERO.compareTo(cash.getInvestedAmount()));
        assertEquals(0, new BigDecimal("50").compareTo(cash.getCurrentValue()));
    }

    @Test
    void record_interestForFdIncreasesCurrentValueOnly() {
        Investment fd = fdInvestment(30L, "FD001", "INR", "10000", "10200");
        when(investmentService.getOrThrow(30L)).thenReturn(fd);
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.INTEREST);
        req.setAmount(new BigDecimal("300"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        transactionService.record(30L, req);

        assertEquals(0, new BigDecimal("10000").compareTo(fd.getInvestedAmount()));
        assertEquals(0, new BigDecimal("10500").compareTo(fd.getCurrentValue()));
    }

    @Test
    void record_buyOnCashThrowsInvalidOperation() {
        Investment cash = cashInvestment(20L, "CASH", "INR", "1000", "1000");
        when(investmentService.getOrThrow(20L)).thenReturn(cash);

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.BUY);
        req.setQuantity(new BigDecimal("1"));
        req.setPrice(new BigDecimal("10"));
        req.setAmount(new BigDecimal("10"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        assertThrows(InvalidOperationException.class, () -> transactionService.record(20L, req));
        verify(investmentRepository, never()).update(any());
    }

    @Test
    void record_depositOnStockThrowsInvalidOperation() {
        Investment stock = stockInvestment(10L, "AAPL", "USD", "2", "100", "200", "200");
        when(investmentService.getOrThrow(10L)).thenReturn(stock);

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.DEPOSIT);
        req.setAmount(new BigDecimal("100"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        assertThrows(InvalidOperationException.class, () -> transactionService.record(10L, req));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void record_requiresPositiveFieldsForOperation() {
        Investment stock = stockInvestment(10L, "AAPL", "USD", "2", "100", "200", "200");
        when(investmentService.getOrThrow(10L)).thenReturn(stock);

        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.SELL);
        req.setQuantity(BigDecimal.ZERO);
        req.setPrice(new BigDecimal("100"));
        req.setAmount(new BigDecimal("1"));
        req.setTransactionDate(LocalDate.of(2026, 8, 5));

        InvalidOperationException ex = assertThrows(InvalidOperationException.class,
                () -> transactionService.record(10L, req));
        assertTrue(ex.getMessage().contains("quantity"));
        verify(transactionRepository, never()).save(any());
    }

    private Investment stockInvestment(Long id,
                                       String symbol,
                                       String currency,
                                       String quantity,
                                       String avgBuyPrice,
                                       String investedAmount,
                                       String currentValue) {
        return Investment.builder()
                .id(id)
                .type(InvestmentType.STOCK)
                .symbol(symbol)
                .currency(currency)
                .quantity(new BigDecimal(quantity))
                .avgBuyPrice(new BigDecimal(avgBuyPrice))
                .investedAmount(new BigDecimal(investedAmount))
                .currentValue(new BigDecimal(currentValue))
                .status(InvestmentStatus.ACTIVE)
                .build();
    }

    private Investment cashInvestment(Long id, String symbol, String currency, String investedAmount, String currentValue) {
        return Investment.builder()
                .id(id)
                .type(InvestmentType.CASH)
                .symbol(symbol)
                .currency(currency)
                .investedAmount(new BigDecimal(investedAmount))
                .currentValue(new BigDecimal(currentValue))
                .status(InvestmentStatus.ACTIVE)
                .build();
    }

    private Investment fdInvestment(Long id, String symbol, String currency, String investedAmount, String currentValue) {
        return Investment.builder()
                .id(id)
                .type(InvestmentType.FD)
                .symbol(symbol)
                .currency(currency)
                .investedAmount(new BigDecimal(investedAmount))
                .currentValue(new BigDecimal(currentValue))
                .status(InvestmentStatus.ACTIVE)
                .build();
    }

    private Transaction tx(Long id,
                           Long investmentId,
                           TransactionType type,
                           String quantity,
                           String price,
                           String amount,
                           String realizedPl) {
        return Transaction.builder()
                .id(id)
                .investmentId(investmentId)
                .type(type)
                .quantity(quantity == null ? null : new BigDecimal(quantity))
                .price(price == null ? null : new BigDecimal(price))
                .amount(amount == null ? null : new BigDecimal(amount))
                .realizedPl(realizedPl == null ? null : new BigDecimal(realizedPl))
                .currency("USD")
                .transactionDate(LocalDate.of(2026, 8, 5))
                .notes("note")
                .build();
    }
}
