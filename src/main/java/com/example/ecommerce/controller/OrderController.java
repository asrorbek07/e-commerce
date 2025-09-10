package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.OrderRequest;
import com.example.ecommerce.dto.response.ApiResponse;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.model.vo.OrderStatus;
import com.example.ecommerce.security.UserPrincipal;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request,
                                                    @AuthenticationPrincipal UserPrincipal currentUser) {
        OrderResponse order = orderService.placeOrder(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Page<OrderResponse> orders = orderService.getUserOrders(currentUser.getId(), page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<OrderResponse> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        OrderResponse order = orderService.getOrderById(id, currentUser.getId());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> getOrderByIdAdmin(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderByIdAdmin(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                           @RequestParam OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserPrincipal currentUser) {
        orderService.cancelOrder(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Order canceled successfully"));
    }
}