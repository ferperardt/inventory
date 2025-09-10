package com.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Integer minStockLevel,
        String category,
        Boolean active,
        Boolean lowStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SupplierResponse> suppliers
) {
}
