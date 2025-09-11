package com.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create a new product")
public record CreateProductRequest(

        @Schema(description = "Product name", example = "Wireless Mouse Logitech MX Master 3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Product name is required")
        @Size(max = 100, message = "Product name must not exceed 100 characters")
        String name,

        @Schema(description = "Product description", example = "Ergonomic wireless mouse with advanced tracking and customizable buttons")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Stock Keeping Unit - unique product identifier", example = "WM-LOG-MX3-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
        String sku,

        @Schema(description = "Product unit price", example = "129.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,

        @Schema(description = "Current stock quantity", example = "50", defaultValue = "0")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        @Schema(description = "Minimum stock level for low stock alerts", example = "10", defaultValue = "0")
        @Min(value = 0, message = "Minimum stock level cannot be negative")
        Integer minStockLevel,

        @Schema(description = "Product category", example = "electronics")
        @Size(max = 50, message = "Category must not exceed 50 characters")
        String category,

        @Schema(description = "List of supplier IDs associated with this product", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]", requiredMode = Schema.RequiredMode.REQUIRED)
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
