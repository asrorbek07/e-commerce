package com.example.ecommerce.util.builder;

import com.example.ecommerce.dto.request.OrderRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.User;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public final class OrderBuilder {

    public Order fromOrderRequest(OrderRequest request, User user) {
        return Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .total(BigDecimal.ZERO)
                .build();
    }


    public String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }
}