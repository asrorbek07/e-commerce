package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllActive(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.isActive = true")
    Page<Product> findByCategoryAndActive(@Param("category") String category, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isActive = true")
    Page<Product> findByNameContainingAndActive(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    Page<Product> findByPriceBetweenAndActive(@Param("minPrice") BigDecimal minPrice, 
                                            @Param("maxPrice") BigDecimal maxPrice, 
                                            Pageable pageable);
    
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdWithLock(@Param("ids") List<Long> ids);
    
    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.isActive = true ORDER BY p.stock ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.isActive = true ORDER BY p.category")
    List<String> findAllCategories();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.isActive = true")
    long countActiveProducts();
}