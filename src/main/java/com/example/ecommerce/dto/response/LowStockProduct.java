package com.example.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProduct {
    private Long productId;
    private String productName;
    private Integer currentStock;
    private String category;
    private BigDecimal price;
}