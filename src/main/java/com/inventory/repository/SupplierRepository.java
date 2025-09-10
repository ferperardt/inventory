package com.inventory.repository;

import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID>, JpaSpecificationExecutor<Supplier> {

    Page<Supplier> findByActiveTrue(Pageable pageable);

    boolean existsByBusinessIdAndActiveTrue(String businessId);

    @Query("SELECT p FROM Supplier s JOIN s.products p WHERE s.id = :supplierId AND s.active = true AND p.active = true")
    Page<Product> findActiveProductsBySupplierId(@Param("supplierId") UUID supplierId, Pageable pageable);
}