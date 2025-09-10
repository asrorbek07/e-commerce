package com.example.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReport {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalDeliveredOrders;
    private BigDecimal averageOrderValue;
    private LocalDateTime reportGeneratedAt;
    private String period;
}