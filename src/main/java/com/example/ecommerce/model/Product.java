package com.example.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Long version;
    
    @Column(nullable = false,length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;
    
    @Column(length = 100)
    private String category;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + this.stock + ", Requested: " + quantity);
        }
        this.stock -= quantity;
    }
    
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}