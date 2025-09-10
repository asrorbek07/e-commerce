package com.example.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private RevenueReport weeklyRevenue;
    private RevenueReport monthlyRevenue;
    private TopSellingProductsResponse topSellingProducts;
    private LowStockResponse criticalStockProducts;
}