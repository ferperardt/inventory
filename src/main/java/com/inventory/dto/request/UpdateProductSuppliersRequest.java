package com.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record UpdateProductSuppliersRequest(

        @NotEmpty(message = "At least one supplier is required")
        List<UUID> supplierIds

) {
}