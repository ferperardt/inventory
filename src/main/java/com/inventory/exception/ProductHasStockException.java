package com.inventory.exception;

import java.util.UUID;

public class ProductHasStockException extends RuntimeException {

    public ProductHasStockException(UUID productId, Integer currentStock) {
        super(String.format("Cannot delete product %s. Current stock: %d. Stock must be zero before deletion.", 
                productId, currentStock));
    }

    public ProductHasStockException(String sku, Integer currentStock) {
        super(String.format("Cannot delete product %s. Current stock: %d. Stock must be zero before deletion.", 
                sku, currentStock));
    }
}