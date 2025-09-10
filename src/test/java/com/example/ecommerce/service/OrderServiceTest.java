package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.OrderItemRequest;
import com.example.ecommerce.dto.request.OrderRequest;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.model.vo.OrderStatus;
import com.example.ecommerce.model.vo.Role;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.util.checker.OrderChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderChecker orderChecker;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private OrderRequest orderRequest;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .isActive(true)
                .build();

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(itemRequest));

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.valueOf(200))
                .build();
    }

    @Test
    void placeOrder_Success() {
        when(orderChecker.checkUserExists(1L)).thenReturn(testUser);
        when(productRepository.findAllByIdWithLock(anyList())).thenReturn(Arrays.asList(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderResponse result = orderService.placeOrder(orderRequest, 1L);

        assertNotNull(result);
        verify(orderChecker).checkUserExists(1L);
        verify(orderChecker).checkAllProductsExist(anyList(), anyList());
        verify(orderChecker).checkProductAvailability(testProduct);
        verify(orderChecker).checkStockAvailability(testProduct, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_Success() {
        OrderItem orderItem = OrderItem.builder()
                .product(testProduct)
                .quantity(2)
                .build();
        testOrder.setItems(Arrays.asList(orderItem));

        when(orderChecker.checkOrderExistsWithItems(1L)).thenReturn(testOrder);

        orderService.cancelOrder(1L, 1L);

        verify(orderChecker).checkUserOwnsOrder(testOrder, 1L);
        verify(orderChecker).checkOrderCanBeCanceled(testOrder);
        verify(productRepository).save(testProduct);
        verify(orderRepository).save(testOrder);
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
    }
}