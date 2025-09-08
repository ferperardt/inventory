package com.inventory.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(UUID productId, Integer currentStock, Integer requestedQuantity) {
        super(String.format("Insufficient stock for product %s. Current stock: %d, requested: %d", 
                productId, currentStock, requestedQuantity));
    }

    public InsufficientStockException(String sku, Integer currentStock, Integer requestedQuantity) {
        super(String.format("Insufficient stock for product %s. Current stock: %d, requested: %d", 
                sku, currentStock, requestedQuantity));
    }
}