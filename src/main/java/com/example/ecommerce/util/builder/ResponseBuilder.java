package com.example.ecommerce.util.builder;

import com.example.ecommerce.dto.response.*;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public final class ResponseBuilder {

    public JwtAuthenticationResponse createJwtResponse(String accessToken, User user) {
        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .user(createUserResponse(user))
                .build();
    }

    public TopSellingProductsResponse createTopSellingProductsResponse(
            List<TopSellingProduct> products, int days) {
        return TopSellingProductsResponse.builder()
                .products(products)
                .period("Last " + days + " days")
                .reportGeneratedAt(LocalDateTime.now())
                .build();
    }

    public TopSellingProduct createTopSellingProduct(Long productId, String productName,
                                                     Long totalSold, BigDecimal totalRevenue) {
        return TopSellingProduct.builder()
                .productId(productId)
                .productName(productName)
                .totalSold(totalSold)
                .totalRevenue(totalRevenue)
                .build();
    }

    public LowStockResponse createLowStockResponse(List<LowStockProduct> products, int threshold) {
        return LowStockResponse.builder()
                .products(products)
                .threshold(threshold)
                .reportGeneratedAt(LocalDateTime.now())
                .build();
    }

    public LowStockProduct createLowStockProduct(Product product) {
        return LowStockProduct.builder()
                .productId(product.getId())
                .productName(product.getName())
                .currentStock(product.getStock())
                .category(product.getCategory())
                .price(product.getPrice())
                .build();
    }

    public RevenueReport createRevenueReport(BigDecimal totalRevenue, Long totalOrders,
                                             Long deliveredOrders, BigDecimal averageOrderValue, int days) {
        return RevenueReport.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalDeliveredOrders(deliveredOrders)
                .averageOrderValue(averageOrderValue)
                .period("Last " + days + " days")
                .reportGeneratedAt(LocalDateTime.now())
                .build();
    }

    public DashboardResponse createDashboardResponse(RevenueReport weeklyRevenue,
                                                     TopSellingProductsResponse topProducts,
                                                     LowStockResponse criticalStockProducts) {
        return DashboardResponse.builder()
                .weeklyRevenue(weeklyRevenue)
                .topSellingProducts(topProducts)
                .criticalStockProducts(criticalStockProducts)
                .build();
    }

    public DashboardResponse createDashboardResponse(RevenueReport weeklyRevenue,
                                                     RevenueReport monthlyRevenue,
                                                     TopSellingProductsResponse topProducts,
                                                     LowStockResponse criticalStock) {
        return DashboardResponse.builder()
                .weeklyRevenue(weeklyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .topSellingProducts(topProducts)
                .criticalStockProducts(criticalStock)
                .build();
    }

    public ProductResponse createProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public OrderResponse createOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .total(order.getTotal())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems().stream()
                        .map(ResponseBuilder::createOrderItemResponse)
                        .toList())
                .build();
    }

    public OrderItemResponse createOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    public UserResponse createUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}