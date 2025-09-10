package com.inventory.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 100, message = "Product name must not exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
        String sku,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        @Min(value = 0, message = "Minimum stock level cannot be negative")
        Integer minStockLevel,

        @Size(max = 50, message = "Category must not exceed 50 characters")
        String category,

        @NotEmpty(message = "At least one supplier is required")
        List<UUID> supplierIds
) {
    // Construtor compacto para valores padrão
    public CreateProductRequest {
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
        if (minStockLevel == null) {
            minStockLevel = 0;
        }
    }
}
