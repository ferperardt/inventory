package com.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Product information with current stock and supplier details")
public record ProductResponse(
        @Schema(description = "Product unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "Product name", example = "Wireless Mouse Logitech MX Master 3")
        String name,
        @Schema(description = "Product description", example = "Ergonomic wireless mouse with advanced tracking")
        String description,
        @Schema(description = "Stock Keeping Unit", example = "WM-LOG-MX3-001")
        String sku,
        @Schema(description = "Current unit price", example = "129.99")
        BigDecimal price,
        @Schema(description = "Current stock quantity", example = "45")
        Integer stockQuantity,
        @Schema(description = "Minimum stock level for alerts", example = "10")
        Integer minStockLevel,
        @Schema(description = "Product category", example = "electronics")
        String category,
        @Schema(description = "Product active status", example = "true")
        Boolean active,
        @Schema(description = "Low stock indicator", example = "false")
        Boolean lowStock,
        @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update timestamp", example = "2024-01-20T14:45:00")
        LocalDateTime updatedAt,
        @Schema(description = "Associated suppliers")
        List<SupplierResponse> suppliers
) {
}
