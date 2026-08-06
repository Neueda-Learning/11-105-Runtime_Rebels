package com.portfoliomanager.service;

import com.portfoliomanager.dto.CommodityRequest;
import com.portfoliomanager.dto.CommodityResponse;
import com.portfoliomanager.dto.InvestmentResponse;
import com.portfoliomanager.model.Commodity;
import com.portfoliomanager.model.CommodityType;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.repository.CommodityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommodityServiceTest {

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private InvestmentService investmentService;

    private CommodityService commodityService;

    @BeforeEach
    void setUp() {
        commodityService = new CommodityService(commodityRepository, investmentService);
    }

    @Test
    void create_createsCommodityBackedInvestment() {
        CommodityRequest request = baseRequest();

        InvestmentResponse createdInvestment = InvestmentResponse.builder()
                .id(10L)
                .type(InvestmentType.COMMODITY)
                .investedAmount(new BigDecimal("2000"))
                .currentValue(new BigDecimal("2200"))
                .build();

        Commodity commodity = baseCommodity();
        commodity.setInvestmentId(10L);

        when(investmentService.create(any())).thenReturn(createdInvestment);
        when(commodityRepository.findByInvestmentId(10L)).thenReturn(Optional.of(commodity));

        CommodityResponse response = commodityService.create(request);

        assertEquals(10L, response.getInvestmentId());
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getProfitLoss()));
        assertEquals(0, new BigDecimal("10.00").compareTo(response.getProfitLossPercent()));
    }

    @Test
    void findAll_mapsFromRepository() {
        Commodity commodity = baseCommodity();
        commodity.setId(1L);
        commodity.setInvestmentId(10L);

        when(commodityRepository.findAll()).thenReturn(List.of(commodity));
        when(investmentService.findById(10L)).thenReturn(InvestmentResponse.builder()
                .id(10L)
                .investedAmount(new BigDecimal("2000"))
                .currentValue(new BigDecimal("2100"))
                .build());

        List<CommodityResponse> all = commodityService.findAll();

        assertEquals(1, all.size());
        assertEquals(1L, all.get(0).getId());
        assertEquals(0, new BigDecimal("100.00").compareTo(all.get(0).getProfitLoss()));
    }

    @Test
    void delete_deletesViaInvestmentService() {
        Commodity commodity = baseCommodity();
        commodity.setId(2L);
        commodity.setInvestmentId(22L);

        when(commodityRepository.findById(2L)).thenReturn(Optional.of(commodity));

        commodityService.delete(2L);

        verify(investmentService).delete(22L);
    }

    private CommodityRequest baseRequest() {
        CommodityRequest req = new CommodityRequest();
        req.setCommodityName("Gold ETF");
        req.setCommodityType(CommodityType.GOLD);
        req.setMarketExchange("MCX");
        req.setCountry("India");
        req.setCurrency("INR");
        req.setQuantity(new BigDecimal("2"));
        req.setPurchasePrice(new BigDecimal("1000"));
        req.setCurrentPrice(new BigDecimal("1100"));
        req.setPurchaseDate(LocalDate.of(2026, 8, 1));
        return req;
    }

    private Commodity baseCommodity() {
        return Commodity.builder()
                .id(99L)
                .commodityName("Gold ETF")
                .commodityType(CommodityType.GOLD)
                .marketExchange("MCX")
                .country("India")
                .currency("INR")
                .quantity(new BigDecimal("2"))
                .purchasePrice(new BigDecimal("1000"))
                .currentPrice(new BigDecimal("1100"))
                .purchaseDate(LocalDate.of(2026, 8, 1))
                .build();
    }
}


