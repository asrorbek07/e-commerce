package com.example.ecommerce.integration;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100))
                .stock(20)
                .category("Electronics")
                .isActive(true)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void getAllProducts_Success() throws Exception {
        Product product2 = Product.builder()
                .name("Product 2")
                .description("Description 2")
                .price(BigDecimal.valueOf(50))
                .stock(5)
                .category("Books")
                .isActive(true)
                .build();
        productRepository.save(product2);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Product 2"))
                .andExpect(jsonPath("$.content[1].name").value("Test Product"));
    }

    @Test
    void searchProducts_ByName_Success() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("name", "Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    void searchProducts_ByCategory_Success() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("category", "Electronics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].category").value("Electronics"));
    }

    @Test
    void getProductById_Success() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(100.0))
                .andExpect(jsonPath("$.data.stock").value(20))
                .andExpect(jsonPath("$.data.category").value("Electronics"));
    }

    @Test
    void getProductById_NotFound_Fails() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Product not found")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createProduct_Success_CompletesFullFlow() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("New Product");
        productRequest.setDescription("New Description");
        productRequest.setPrice(BigDecimal.valueOf(200));
        productRequest.setStock(15);
        productRequest.setCategory("Books");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Product"))
                .andExpect(jsonPath("$.data.price").value(200.0))
                .andExpect(jsonPath("$.data.stock").value(15))
                .andExpect(jsonPath("$.data.category").value("Books"));

        assertEquals(2, productRepository.count());
        Product savedProduct = productRepository.findAll().stream()
                .filter(p -> "New Product".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        
        assertEquals("New Description", savedProduct.getDescription());
        assertEquals(BigDecimal.valueOf(200), savedProduct.getPrice());
        assertEquals(15, savedProduct.getStock());
        assertEquals("Books", savedProduct.getCategory());
        assertTrue(savedProduct.getIsActive());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void createProduct_InsufficientPermissions_Fails() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Unauthorized Product");
        productRequest.setPrice(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());

        assertEquals(1, productRepository.count());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateProduct_Success() throws Exception {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(BigDecimal.valueOf(150));
        updateRequest.setStock(25);
        updateRequest.setCategory("Gaming");

        mockMvc.perform(put("/api/products/{id}", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Product"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"))
                .andExpect(jsonPath("$.data.price").value(150.0))
                .andExpect(jsonPath("$.data.stock").value(25))
                .andExpect(jsonPath("$.data.category").value("Gaming"));

        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals("Updated Description", updatedProduct.getDescription());
        assertEquals(BigDecimal.valueOf(150), updatedProduct.getPrice());
        assertEquals(25, updatedProduct.getStock());
        assertEquals("Gaming", updatedProduct.getCategory());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void deleteProduct_Success_SoftDelete() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isNoContent());

        Product deletedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertFalse(deletedProduct.getIsActive());
    }

    @Test
    void getAllCategories_Success() throws Exception {
        Product product2 = Product.builder()
                .name("Book Product")
                .description("A book")
                .price(BigDecimal.valueOf(30))
                .stock(10)
                .category("Books")
                .isActive(true)
                .build();
        productRepository.save(product2);

        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*]").value(org.hamcrest.Matchers.containsInAnyOrder("Electronics", "Books")));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getLowStockProducts_Success() throws Exception {
        Product lowStockProduct = Product.builder()
                .name("Low Stock Product")
                .description("Running out")
                .price(BigDecimal.valueOf(75))
                .stock(3)
                .category("Electronics")
                .isActive(true)
                .build();
        productRepository.save(lowStockProduct);

        mockMvc.perform(get("/api/products/low-stock")
                        .param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Low Stock Product"))
                .andExpect(jsonPath("$[0].stock").value(3));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getActiveProductCount_Success() throws Exception {
        Product product2 = Product.builder()
                .name("Second Product")
                .description("Another product")
                .price(BigDecimal.valueOf(60))
                .stock(8)
                .category("Books")
                .isActive(true)
                .build();
        productRepository.save(product2);

        Product inactiveProduct = Product.builder()
                .name("Inactive Product")
                .description("Not active")
                .price(BigDecimal.valueOf(90))
                .stock(12)
                .category("Electronics")
                .isActive(false)
                .build();
        productRepository.save(inactiveProduct);

        mockMvc.perform(get("/api/products/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }
}