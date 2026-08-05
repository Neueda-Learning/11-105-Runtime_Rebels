package com.portfoliomanager.controller;

import com.portfoliomanager.dto.TransactionRequest;
import com.portfoliomanager.dto.TransactionResponse;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private TransactionController controller;

    @BeforeEach
    void setUp() {
        controller = new TransactionController(transactionService);
    }

    @Test
    void findAll_returnsTransactionsFromService() {
        TransactionResponse tx = TransactionResponse.builder().id(1L).build();
        when(transactionService.findAll()).thenReturn(List.of(tx));

        List<TransactionResponse> result = controller.findAll();

        assertEquals(1, result.size());
        assertSame(tx, result.get(0));
        verify(transactionService).findAll();
    }

    @Test
    void findByInvestment_returnsTransactionsFromService() {
        TransactionResponse tx = TransactionResponse.builder().id(2L).build();
        when(transactionService.findByInvestment(10L)).thenReturn(List.of(tx));

        List<TransactionResponse> result = controller.findByInvestment(10L);

        assertEquals(1, result.size());
        assertSame(tx, result.get(0));
        verify(transactionService).findByInvestment(10L);
    }

    @Test
    void record_returnsCreatedResponseEntity() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.BUY);
        request.setAmount(java.math.BigDecimal.ONE);
        request.setTransactionDate(LocalDate.of(2026, 8, 5));

        TransactionResponse created = TransactionResponse.builder().id(3L).build();
        when(transactionService.record(10L, request)).thenReturn(created);

        ResponseEntity<TransactionResponse> result = controller.record(10L, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(created, result.getBody());
        verify(transactionService).record(10L, request);
    }
}
