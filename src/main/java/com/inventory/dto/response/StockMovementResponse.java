package com.inventory.dto.response;

import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Stock movement record with product details and stock changes")
public record StockMovementResponse(
        @Schema(description = "Movement unique identifier", example = "789e4567-e89b-12d3-a456-426614174002")
        UUID id,
        @Schema(description = "Product identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID productId,
        @Schema(description = "Product SKU", example = "WM-LOG-MX3-001")
        String productSku,
        @Schema(description = "Product name", example = "Wireless Mouse Logitech MX Master 3")
        String productName,
        @Schema(description = "Type of movement", example = "IN")
        MovementType movementType,
        @Schema(description = "Quantity moved", example = "25")
        Integer quantity,
        @Schema(description = "Stock level before movement", example = "20")
        Integer previousStock,
        @Schema(description = "Stock level after movement", example = "45")
        Integer newStock,
        @Schema(description = "Reason for movement", example = "PURCHASE")
        MovementReason reason,
        @Schema(description = "External reference", example = "PO-2024-001")
        String reference,
        @Schema(description = "Additional notes", example = "Bulk purchase for Q1 inventory")
        String notes,
        @Schema(description = "User who created the movement", example = "system")
        String createdBy,
        @Schema(description = "Movement timestamp", example = "2024-01-20T11:30:00")
        LocalDateTime createdAt
) {
}