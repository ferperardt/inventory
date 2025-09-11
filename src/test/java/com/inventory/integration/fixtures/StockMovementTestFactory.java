package com.inventory.integration.fixtures;

import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;

import java.util.UUID;

public class StockMovementTestFactory {

    public static CreateStockMovementRequest validInMovementRequest(UUID productId) {
        return new CreateStockMovementRequest(
                productId,
                MovementType.IN,
                10,
                MovementReason.PURCHASE,
                "PO-" + System.currentTimeMillis(),
                "Stock movement for integration tests"
        );
    }

    public static CreateStockMovementRequest validOutMovementRequest(UUID productId, Integer quantity) {
        return new CreateStockMovementRequest(
                productId,
                MovementType.OUT,
                quantity,
                MovementReason.SALE,
                "SO-" + System.currentTimeMillis(),
                "Stock out movement for integration tests"
        );
    }

    public static CreateStockMovementRequest validOutMovementRequest(UUID productId) {
        return validOutMovementRequest(productId, 5);
    }

    public static CreateStockMovementRequest adjustmentMovementRequest(UUID productId, Integer quantity, MovementType type) {
        return new CreateStockMovementRequest(
                productId,
                type,
                quantity,
                MovementReason.ADJUSTMENT,
                "ADJ-" + System.currentTimeMillis(),
                "Stock adjustment for integration tests"
        );
    }

    public static CreateStockMovementRequest insufficientStockMovementRequest(UUID productId, Integer excessiveQuantity) {
        return new CreateStockMovementRequest(
                productId,
                MovementType.OUT,
                excessiveQuantity,
                MovementReason.SALE,
                "FAIL-" + System.currentTimeMillis(),
                "Movement that should fail due to insufficient stock"
        );
    }

    public static CreateStockMovementRequest customMovementRequest(
            UUID productId, 
            MovementType type, 
            Integer quantity, 
            MovementReason reason, 
            String reference) {
        return new CreateStockMovementRequest(
                productId,
                type,
                quantity,
                reason,
                reference,
                "Custom movement for integration tests"
        );
    }
}