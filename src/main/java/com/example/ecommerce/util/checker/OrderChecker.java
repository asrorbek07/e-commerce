package com.example.ecommerce.util.checker;

import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.model.vo.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderChecker {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public void checkAllProductsExist(List<Long> requestedProductIds, List<Product> foundProducts) {
        if (foundProducts.size() != requestedProductIds.size()) {
            throw new BadRequestException("One or more products not found");
        }
    }

    public void checkProductAvailability(Product product) {
        if (!product.getIsActive()) {
            throw new BadRequestException("Product " + product.getName() + " is not available");
        }
    }

    public void checkStockAvailability(Product product, Integer requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() +
                            ". Available: " + product.getStock() + ", Requested: " + requestedQuantity
            );
        }
    }

    public void checkUserOwnsOrder(Order order, Long userId) {
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied");
        }
    }

    public void checkOrderCanBeCanceled(Order order) {
        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel order in " + order.getStatus() + " status");
        }
    }

    public User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    public Order checkOrderExists(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    public Order checkOrderExistsWithItems(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }
}