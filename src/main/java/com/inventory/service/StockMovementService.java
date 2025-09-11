package com.inventory.service;

import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.mapper.StockMovementMapper;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;
    private final ProductRepository productRepository;

    public StockMovementService(StockMovementRepository stockMovementRepository,
                                StockMovementMapper stockMovementMapper,
                                ProductRepository productRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getAllMovements(Pageable pageable) {
        return stockMovementRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)
                .map(stockMovementMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getMovementsByProductId(UUID productId, Pageable pageable) {
        // Verify product exists and is active
        productRepository.findById(productId)
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return stockMovementRepository.findByProductIdAndActiveTrueOrderByCreatedAtDesc(productId, pageable)
                .map(stockMovementMapper::toResponse);
    }

    @Transactional
    public StockMovementResponse createStockMovement(CreateStockMovementRequest request) {
        // Find and validate product exists and is active
        Product product = productRepository.findById(request.productId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        // Get current stock
        Integer currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

        Integer newStock;

        // Special handling for INITIAL_STOCK - sets stock directly, doesn't add/subtract
        if (request.reason() == MovementReason.INITIAL_STOCK) {
            newStock = request.quantity();
        } else {
            // Calculate new stock based on movement type for regular movements
            if (request.movementType() == MovementType.IN) {
                newStock = currentStock + request.quantity();
            } else { // MovementType.OUT
                newStock = currentStock - request.quantity();

                // Validate sufficient stock for OUT movements
                if (newStock < 0) {
                    throw new InsufficientStockException(product.getSku(), currentStock, request.quantity());
                }
            }
        }

        // Create stock movement record
        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setMovementType(request.movementType());
        stockMovement.setQuantity(request.quantity());
        stockMovement.setPreviousStock(currentStock);
        stockMovement.setNewStock(newStock);
        stockMovement.setReason(request.reason());
        stockMovement.setReference(request.reference());
        stockMovement.setNotes(request.notes());
        stockMovement.setCreatedBy("system"); // TODO: Get from security context

        // Update product stock
        product.setStockQuantity(newStock);

        // Save both entities
        productRepository.save(product);
        StockMovement savedMovement = stockMovementRepository.save(stockMovement);

        return stockMovementMapper.toResponse(savedMovement);
    }
}