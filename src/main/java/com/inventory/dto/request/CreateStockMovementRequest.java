package com.inventory.dto.request;

import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Request to create a stock movement")
public record CreateStockMovementRequest(

        @Schema(description = "Product identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Product ID is required")
        UUID productId,

        @Schema(description = "Type of movement", example = "IN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Movement type is required")
        MovementType movementType,

        @Schema(description = "Quantity moved", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity,

        @Schema(description = "Reason for movement", example = "PURCHASE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Reason is required")
        MovementReason reason,

        @Schema(description = "External reference (order number, invoice, etc)", example = "PO-2024-001")
        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Schema(description = "Additional notes", example = "Bulk purchase for Q1 inventory")
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}