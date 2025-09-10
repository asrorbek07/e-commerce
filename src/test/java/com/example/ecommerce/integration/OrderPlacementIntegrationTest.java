package com.example.ecommerce.integration;

import com.example.ecommerce.dto.request.OrderItemRequest;
import com.example.ecommerce.dto.request.OrderRequest;
import com.example.ecommerce.model.*;
import com.example.ecommerce.model.vo.OrderStatus;
import com.example.ecommerce.model.vo.Role;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class OrderPlacementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);

        testProduct1 = Product.builder()
                .name("Product 1")
                .description("Description 1")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .category("Electronics")
                .isActive(true)
                .build();
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = Product.builder()
                .name("Product 2")
                .description("Description 2")
                .price(BigDecimal.valueOf(50))
                .stock(5)
                .category("Books")
                .isActive(true)
                .build();
        testProduct2 = productRepository.save(testProduct2);
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"USER"})
    void placeOrder_Success_CompletesFullFlow() throws Exception {
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getId());
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getId());
        item2.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(item1, item2));

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.total").value(250.0))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);

        Product updatedProduct1 = productRepository.findById(testProduct1.getId()).orElseThrow();
        Product updatedProduct2 = productRepository.findById(testProduct2.getId()).orElseThrow();
        
        assertEquals(8, updatedProduct1.getStock());
        assertEquals(4, updatedProduct2.getStock());

        assertEquals(1, orderRepository.count());
        Order savedOrder = orderRepository.findAll().get(0);
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(BigDecimal.valueOf(250), savedOrder.getTotal());
        assertEquals(2, savedOrder.getItems().size());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"USER"})
    void placeOrder_InsufficientStock_FailsGracefully() throws Exception {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(testProduct2.getId());
        item.setQuantity(10);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(item));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient stock")));

        Product unchangedProduct = productRepository.findById(testProduct2.getId()).orElseThrow();
        assertEquals(5, unchangedProduct.getStock());
        assertEquals(0, orderRepository.count());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"USER"})
    void getUserOrders_Success() throws Exception {
        Order testOrder = Order.builder()
                .user(testUser)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.valueOf(100))
                .orderNumber("ORD-12345")
                .build();
        orderRepository.save(testOrder);

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].total").value(100.0));
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"USER"})
    void cancelOrder_Success_RestoresStock() throws Exception {
        OrderItem orderItem = OrderItem.builder()
                .product(testProduct1)
                .quantity(3)
                .price(testProduct1.getPrice())
                .build();

        Order testOrder = Order.builder()
                .user(testUser)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.valueOf(300))
                .orderNumber("ORD-12345")
                .build();
        testOrder.addOrderItem(orderItem);
        testOrder = orderRepository.save(testOrder);

        testProduct1.decreaseStock(3);
        productRepository.save(testProduct1);

        mockMvc.perform(put("/api/orders/{id}/cancel", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order canceled successfully"));

        Order canceledOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, canceledOrder.getStatus());

        Product restoredProduct = productRepository.findById(testProduct1.getId()).orElseThrow();
        assertEquals(10, restoredProduct.getStock());
    }
}