package com.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(unique = true, nullable = false, length = 100)
    private String sku;

    @Column(name = "original_sku", length = 50)
    private String originalSku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer minStockLevel;

    @Column(length = 50)
    private String category;

    public Product(String name, String sku, BigDecimal price) {
        this.name = name;
        this.sku = sku;
        this.price = price;
    }
    
    public boolean isLowStock() {
        return stockQuantity != null && minStockLevel != null && stockQuantity <= minStockLevel;
    }

    @Override
    public void softDelete() {
        super.softDelete();
        // Store original SKU and modify current SKU to allow reuse
        if (this.originalSku == null) {
            this.originalSku = this.sku;
        }
        this.sku = this.sku + "_deleted_" + System.currentTimeMillis();
    }


}