package com.inventory.exception;

public class DuplicateBusinessIdException extends RuntimeException {
    
    public DuplicateBusinessIdException(String businessId) {
        super("Supplier with Business ID '" + businessId + "' already exists");
    }
    
    public DuplicateBusinessIdException(String message, Throwable cause) {
        super(message, cause);
    }
}