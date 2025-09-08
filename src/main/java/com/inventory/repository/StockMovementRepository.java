package com.inventory.repository;

import com.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm JOIN FETCH sm.product WHERE sm.active = true ORDER BY sm.createdAt DESC")
    Page<StockMovement> findAllActiveWithProduct(Pageable pageable);
}