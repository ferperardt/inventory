package com.inventory.exception;

public class DuplicateSkuException extends RuntimeException {
    
    public DuplicateSkuException(String sku) {
        super("Product with SKU '" + sku + "' already exists");
    }
    
    public DuplicateSkuException(String message, Throwable cause) {
        super(message, cause);
    }
}