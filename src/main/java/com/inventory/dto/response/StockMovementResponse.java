package com.inventory.dto.response;

import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        String productSku,
        String productName,
        MovementType movementType,
        Integer quantity,
        Integer previousStock,
        Integer newStock,
        MovementReason reason,
        String reference,
        String notes,
        String createdBy,
        LocalDateTime createdAt
) {
}