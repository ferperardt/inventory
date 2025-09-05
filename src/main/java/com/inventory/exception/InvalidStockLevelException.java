package com.inventory.exception;

public class InvalidStockLevelException extends RuntimeException {
    public InvalidStockLevelException(String message) {
        super(message);
    }
}