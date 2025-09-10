package com.example.ecommerce.util.builder;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.model.Product;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductBuilder {

    public Product fromProductRequest(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .build();
    }

}