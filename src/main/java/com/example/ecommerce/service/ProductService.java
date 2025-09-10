package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.util.builder.ProductBuilder;
import com.example.ecommerce.util.builder.ResponseBuilder;
import com.example.ecommerce.util.checker.ProductChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductChecker productChecker;

    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAllActive(pageable)
                .map(ResponseBuilder::createProductResponse);
    }

    public Page<ProductResponse> searchProducts(String name, String category,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (name != null && !name.trim().isEmpty()) {
            return productRepository.findByNameContainingAndActive(name, pageable)
                    .map(ResponseBuilder::createProductResponse);
        } else if (category != null && !category.trim().isEmpty()) {
            return productRepository.findByCategoryAndActive(category, pageable)
                    .map(ResponseBuilder::createProductResponse);
        } else if (minPrice != null && maxPrice != null) {
            return productRepository.findByPriceBetweenAndActive(minPrice, maxPrice, pageable)
                    .map(ResponseBuilder::createProductResponse);
        } else {
            return productRepository.findAllActive(pageable)
                    .map(ResponseBuilder::createProductResponse);
        }
    }

    public ProductResponse getProductById(Long id) {
        Product product = productChecker.checkProductExists(id);

        return ResponseBuilder.createProductResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request) {

        Product product = ProductBuilder.fromProductRequest(request);

        product = productRepository.save(product);

        return ResponseBuilder.createProductResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Product product = productChecker.checkProductExists(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());

        product = productRepository.save(product);
        return ResponseBuilder.createProductResponse(product);
    }

    public void deleteProduct(Long id) {

        Product product = productChecker.checkProductExists(id);
        product.setIsActive(false);
        productRepository.save(product);

    }

    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(ResponseBuilder::createProductResponse)
                .collect(Collectors.toList());
    }

    public long getActiveProductCount() {
        return productRepository.countActiveProducts();
    }
}