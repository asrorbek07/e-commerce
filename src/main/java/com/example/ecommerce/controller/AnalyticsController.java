package com.example.ecommerce.controller;

import com.example.ecommerce.util.builder.ResponseBuilder;
import com.example.ecommerce.dto.response.DashboardResponse;
import com.example.ecommerce.dto.response.LowStockResponse;
import com.example.ecommerce.dto.response.RevenueReport;
import com.example.ecommerce.dto.response.TopSellingProductsResponse;
import com.example.ecommerce.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-products")
    public ResponseEntity<TopSellingProductsResponse> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "30") int days) {

        TopSellingProductsResponse response =
                analyticsService.getTopSellingProducts(limit, days);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<LowStockResponse> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {

        LowStockResponse response =
                analyticsService.getLowStockProducts(threshold);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/revenue-report")
    public ResponseEntity<RevenueReport> getRevenueReport(
            @RequestParam(defaultValue = "30") int days) {

        RevenueReport response = analyticsService.getRevenueReport(days);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardData() {

        RevenueReport weeklyRevenue = analyticsService.getCurrentWeekRevenue();
        RevenueReport monthlyRevenue = analyticsService.getCurrentMonthRevenue();
        TopSellingProductsResponse topProducts = analyticsService.getTopSellingProductsThisMonth();
        LowStockResponse criticalStock = analyticsService.getCriticalStockProducts();

        DashboardResponse dashboard = ResponseBuilder.createDashboardResponse(
                weeklyRevenue, monthlyRevenue, topProducts, criticalStock);

        return ResponseEntity.ok(dashboard);
    }
}