package com.example.ecommerce.util.builder;

import com.example.ecommerce.dto.request.OrderItemRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderItemBuilder {

    public OrderItem fromOrderItemRequest(OrderItemRequest request, Order order, Product product) {
        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .build();
    }

}