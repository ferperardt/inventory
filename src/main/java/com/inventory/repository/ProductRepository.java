package com.inventory.repository;

import com.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Page<Product> findByActiveTrue(Pageable pageable);
    
    Optional<Product> findBySkuAndActiveTrue(String sku);

    // Find by category (active only)
    Page<Product> findByCategoryAndActiveTrue(String category, Pageable pageable);

    // Find products with low stock (active only) - Using custom query since derived method is complex
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockLevel AND p.active = true")
    List<Product> findLowStockActiveProducts();

    // Find products with low stock with pagination (active only)
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minStockLevel AND p.active = true")
    Page<Product> findLowStockActiveProducts(Pageable pageable);

    // Search by name containing (case insensitive, active only)
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    // Check if SKU exists (for validation, active only)
    boolean existsBySkuAndActiveTrue(String sku);
}