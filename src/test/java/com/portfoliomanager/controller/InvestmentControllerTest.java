package com.portfoliomanager.controller;

import com.portfoliomanager.dto.InvestmentRequest;
import com.portfoliomanager.dto.InvestmentResponse;
import com.portfoliomanager.dto.PriceUpdateRequest;
import com.portfoliomanager.model.InvestmentStatus;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.service.InvestmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentControllerTest {

    @Mock
    private InvestmentService investmentService;

    private InvestmentController controller;

    @BeforeEach
    void setUp() {
        controller = new InvestmentController(investmentService);
    }

    @Test
    void findAll_delegatesFiltersToService() {
        InvestmentResponse response = InvestmentResponse.builder().id(1L).build();
        when(investmentService.findAll(InvestmentType.STOCK, "US", InvestmentStatus.ACTIVE))
                .thenReturn(List.of(response));

        List<InvestmentResponse> result = controller.findAll(InvestmentType.STOCK, "US", InvestmentStatus.ACTIVE);

        assertEquals(1, result.size());
        assertSame(response, result.get(0));
        verify(investmentService).findAll(InvestmentType.STOCK, "US", InvestmentStatus.ACTIVE);
    }

    @Test
    void findById_returnsServiceValue() {
        InvestmentResponse response = InvestmentResponse.builder().id(10L).build();
        when(investmentService.findById(10L)).thenReturn(response);

        InvestmentResponse result = controller.findById(10L);

        assertSame(response, result);
        verify(investmentService).findById(10L);
    }

    @Test
    void create_returnsCreatedResponseEntity() {
        InvestmentRequest request = new InvestmentRequest();
        InvestmentResponse created = InvestmentResponse.builder().id(11L).build();
        when(investmentService.create(request)).thenReturn(created);

        ResponseEntity<InvestmentResponse> result = controller.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(created, result.getBody());
        verify(investmentService).create(request);
    }

    @Test
    void update_returnsUpdatedResponse() {
        InvestmentRequest request = new InvestmentRequest();
        InvestmentResponse updated = InvestmentResponse.builder().id(12L).build();
        when(investmentService.update(12L, request)).thenReturn(updated);

        InvestmentResponse result = controller.update(12L, request);

        assertSame(updated, result);
        verify(investmentService).update(12L, request);
    }

    @Test
    void updatePrice_returnsUpdatedResponse() {
        PriceUpdateRequest request = new PriceUpdateRequest();
        InvestmentResponse updated = InvestmentResponse.builder().id(13L).build();
        when(investmentService.updatePrice(13L, request)).thenReturn(updated);

        InvestmentResponse result = controller.updatePrice(13L, request);

        assertSame(updated, result);
        verify(investmentService).updatePrice(13L, request);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> result = controller.delete(99L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(investmentService).delete(99L);
    }
}
