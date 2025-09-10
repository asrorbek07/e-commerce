package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.*;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.util.builder.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public TopSellingProductsResponse getTopSellingProducts(int limit, int days) {

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();

        List<Object[]> rawResults = orderItemRepository.findTopSellingProducts(startDate, endDate);

        List<TopSellingProduct> topProducts = rawResults.stream()
                .limit(limit)
                .map(result -> {
                    Long productId = ((Number) result[0]).longValue();
                    String productName = (String) result[1];
                    Long totalSold = ((Number) result[2]).longValue();
                    BigDecimal totalRevenue = (BigDecimal) result[3];

                    return ResponseBuilder.createTopSellingProduct(productId, productName, totalSold, totalRevenue);
                })
                .collect(Collectors.toList());

        return ResponseBuilder.createTopSellingProductsResponse(topProducts, days);
    }

    public LowStockResponse getLowStockProducts(int threshold) {

        List<Product> lowStockProducts = productRepository.findLowStockProducts(threshold);

        List<LowStockProduct> products = lowStockProducts.stream()
                .map(ResponseBuilder::createLowStockProduct)
                .collect(Collectors.toList());

        return ResponseBuilder.createLowStockResponse(products, threshold);
    }

    public RevenueReport getRevenueReport(int days) {

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();

        BigDecimal totalRevenue = orderRepository.calculateRevenueForPeriod(startDate, endDate);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        Long totalOrders = orderRepository.countOrdersSince(startDate);

        Long deliveredOrders = orderRepository.countDeliveredOrdersBetween(startDate, endDate);

        BigDecimal averageOrderValue = deliveredOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return ResponseBuilder.createRevenueReport(totalRevenue, totalOrders, deliveredOrders, averageOrderValue, days);
    }

    public RevenueReport getCurrentWeekRevenue() {
        return getRevenueReport(7);
    }

    public RevenueReport getCurrentMonthRevenue() {
        return getRevenueReport(30);
    }

    public TopSellingProductsResponse getTopSellingProductsThisMonth() {
        return getTopSellingProducts(10, 30);
    }

    public LowStockResponse getCriticalStockProducts() {
        return getLowStockProducts(5);
    }
}