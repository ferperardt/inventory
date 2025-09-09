package com.inventory.exception;

import java.util.UUID;

public class SupplierNotFoundException extends RuntimeException {
    
    public SupplierNotFoundException(UUID id) {
        super("Supplier not found with id: " + id);
    }
    
    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}