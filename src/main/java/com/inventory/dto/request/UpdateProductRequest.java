package com.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to update product information")
public record UpdateProductRequest(

        @Schema(description = "Product name", example = "Wireless Mouse Logitech MX Master 3S", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Product name is required")
        @Size(max = 100, message = "Product name must not exceed 100 characters")
        String name,

        @Schema(description = "Product description", example = "Enhanced ergonomic wireless mouse with improved tracking")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Schema(description = "Stock Keeping Unit", example = "WM-LOG-MX3S-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
        String sku,

        @Schema(description = "Updated unit price", example = "139.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,

        @Schema(description = "Minimum stock level threshold", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Minimum stock level is required")
        @Min(value = 0, message = "Minimum stock level cannot be negative")
        Integer minStockLevel,

        @Schema(description = "Product category", example = "electronics")
        @Size(max = 50, message = "Category must not exceed 50 characters")
        String category
) {
}