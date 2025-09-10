package com.example.ecommerce.service;

import com.example.ecommerce.util.builder.OrderBuilder;
import com.example.ecommerce.util.builder.OrderItemBuilder;
import com.example.ecommerce.util.builder.ResponseBuilder;
import com.example.ecommerce.util.checker.OrderChecker;
import com.example.ecommerce.dto.request.OrderItemRequest;
import com.example.ecommerce.dto.request.OrderRequest;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.vo.OrderStatus;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderChecker orderChecker;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Transactional
    @Retryable(value = {OptimisticLockingFailureException.class}, 
               maxAttempts = MAX_RETRY_ATTEMPTS, 
               backoff = @Backoff(delay = 100))
    public OrderResponse placeOrder(OrderRequest request, Long userId) {
        
        User user = orderChecker.checkUserExists(userId);
        
        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());
        
        List<Product> products = validateAndLockProducts(productIds, request.getItems());
        
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            product.decreaseStock(itemRequest.getQuantity());
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(itemTotal);
        }
        
        Order order = OrderBuilder.fromOrderRequest(request, user);
        order.setTotal(total);
        order.setOrderNumber(OrderBuilder.generateOrderNumber());
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());
            OrderItem orderItem = OrderItemBuilder.fromOrderItemRequest(itemRequest, order, product);
            order.addOrderItem(orderItem);
        }
        
        order = orderRepository.save(order);
        
        productRepository.saveAll(products);
        
        return ResponseBuilder.createOrderResponse(order);
    }
    
    private List<Product> validateAndLockProducts(List<Long> productIds, 
                                                 List<OrderItemRequest> items) {
        List<Product> products = productRepository.findAllByIdWithLock(productIds);
        
        orderChecker.checkAllProductsExist(productIds, products);
        
        Map<Long, Integer> quantityMap = items.stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getProductId,
                        OrderItemRequest::getQuantity
                ));
        
        for (Product product : products) {
            Integer requestedQuantity = quantityMap.get(product.getId());
            orderChecker.checkProductAvailability(product);
            orderChecker.checkStockAvailability(product, requestedQuantity);
        }
        
        return products;
    }

    public Page<OrderResponse> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByUserId(userId, pageable)
                .map(ResponseBuilder::createOrderResponse);
    }

    public Page<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable)
                .map(ResponseBuilder::createOrderResponse);
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderChecker.checkOrderExistsWithItems(orderId);
        
        orderChecker.checkUserOwnsOrder(order, userId);
        
        return ResponseBuilder.createOrderResponse(order);
    }

    public OrderResponse getOrderByIdAdmin(Long orderId) {
        Order order = orderChecker.checkOrderExistsWithItems(orderId);
        
        return ResponseBuilder.createOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        
        Order order = orderChecker.checkOrderExists(orderId);
        
        order.setStatus(status);
        order = orderRepository.save(order);
        
        return ResponseBuilder.createOrderResponse(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        
        Order order = orderChecker.checkOrderExistsWithItems(orderId);
        
        orderChecker.checkUserOwnsOrder(order, userId);
        
        orderChecker.checkOrderCanBeCanceled(order);
        
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
    }
}