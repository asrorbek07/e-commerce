package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.util.checker.ProductChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductChecker productChecker;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .category("Electronics")
                .isActive(true)
                .build();

        productRequest = new ProductRequest();
        productRequest.setName("New Product");
        productRequest.setDescription("New Description");
        productRequest.setPrice(BigDecimal.valueOf(150));
        productRequest.setStock(20);
        productRequest.setCategory("Books");
    }

    @Test
    void getProductById_Success() {
        when(productChecker.checkProductExists(1L)).thenReturn(testProduct);

        ProductResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals(BigDecimal.valueOf(100), result.getPrice());
    }

    @Test
    void createProduct_Success() {
        Product savedProduct = Product.builder()
                .id(2L)
                .name("New Product")
                .price(BigDecimal.valueOf(150))
                .stock(20)
                .isActive(true)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse result = productService.createProduct(productRequest);

        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals(BigDecimal.valueOf(150), result.getPrice());
    }

    @Test
    void deleteProduct_Success() {
        when(productChecker.checkProductExists(1L)).thenReturn(testProduct);

        productService.deleteProduct(1L);

        assertFalse(testProduct.getIsActive());
        verify(productChecker).checkProductExists(1L);
    }
}