package com.portfoliomanager.controller;

import com.portfoliomanager.dto.CommodityRequest;
import com.portfoliomanager.dto.CommodityResponse;
import com.portfoliomanager.service.CommodityService;
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
class CommodityControllerTest {

    @Mock
    private CommodityService commodityService;

    private CommodityController controller;

    @BeforeEach
    void setUp() {
        controller = new CommodityController(commodityService);
    }

    @Test
    void create_returnsCreated() {
        CommodityRequest request = new CommodityRequest();
        CommodityResponse response = CommodityResponse.builder().id(1L).build();
        when(commodityService.create(request)).thenReturn(response);

        ResponseEntity<CommodityResponse> result = controller.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(commodityService).create(request);
    }

    @Test
    void findAll_returnsList() {
        CommodityResponse item = CommodityResponse.builder().id(1L).build();
        when(commodityService.findAll()).thenReturn(List.of(item));

        List<CommodityResponse> result = controller.findAll();

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
    }

    @Test
    void findById_returnsCommodity() {
        CommodityResponse response = CommodityResponse.builder().id(2L).build();
        when(commodityService.findById(2L)).thenReturn(response);

        CommodityResponse result = controller.findById(2L);

        assertSame(response, result);
    }

    @Test
    void update_returnsCommodity() {
        CommodityRequest request = new CommodityRequest();
        CommodityResponse response = CommodityResponse.builder().id(3L).build();
        when(commodityService.update(3L, request)).thenReturn(response);

        CommodityResponse result = controller.update(3L, request);

        assertSame(response, result);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> result = controller.delete(8L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(commodityService).delete(8L);
    }
}

