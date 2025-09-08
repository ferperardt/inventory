package com.inventory.dto.request;

import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateStockMovementRequest(

        @NotNull(message = "Product ID is required")
        UUID productId,

        @NotNull(message = "Movement type is required")
        MovementType movementType,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity,

        @NotNull(message = "Reason is required")
        MovementReason reason,

        @Size(max = 100, message = "Reference must not exceed 100 characters")
        String reference,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}