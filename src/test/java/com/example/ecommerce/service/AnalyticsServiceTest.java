package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.RevenueReport;
import com.example.ecommerce.dto.response.TopSellingProductsResponse;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Object[] topSellingProductData;

    @BeforeEach
    void setUp() {

        topSellingProductData = new Object[]{1L, "Test Product", 50L, BigDecimal.valueOf(5000)};
    }

    @Test
    void getTopSellingProducts_Success() {
        List<Object[]> mockData = Arrays.asList(new Object[][]{topSellingProductData});
        when(orderItemRepository.findTopSellingProducts(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockData);

        TopSellingProductsResponse result = analyticsService.getTopSellingProducts(10, 30);

        assertNotNull(result);
        assertEquals(1, result.getProducts().size());
        assertEquals("Test Product", result.getProducts().get(0).getProductName());
        assertEquals("Last 30 days", result.getPeriod());
        verify(orderItemRepository).findTopSellingProducts(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getRevenueReport_Success() {
        when(orderRepository.calculateRevenueForPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(10000));
        when(orderRepository.countOrdersSince(any(LocalDateTime.class))).thenReturn(25L);
        when(orderRepository.countDeliveredOrdersBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(20L);

        RevenueReport result = analyticsService.getRevenueReport(30);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(10000), result.getTotalRevenue());
        assertEquals(25L, result.getTotalOrders());
        assertEquals("Last 30 days", result.getPeriod());
    }

    @Test
    void getRevenueReport_NullRevenue_HandledCorrectly() {
        when(orderRepository.calculateRevenueForPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
        when(orderRepository.countOrdersSince(any(LocalDateTime.class))).thenReturn(0L);
        when(orderRepository.countDeliveredOrdersBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        RevenueReport result = analyticsService.getRevenueReport(30);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
        assertEquals(0L, result.getTotalOrders());
    }
}