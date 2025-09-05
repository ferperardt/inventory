package com.inventory.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id);
    }
    
    public ProductNotFoundException(String sku) {
        super("Product not found with SKU: " + sku);
    }
    
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}