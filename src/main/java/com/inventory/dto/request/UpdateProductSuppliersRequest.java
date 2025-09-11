package com.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to update product suppliers association")
public record UpdateProductSuppliersRequest(

        @Schema(description = "List of supplier IDs to associate with the product", example = "[\"123e4567-e89b-12d3-a456-426614174000\", \"456e1234-e89b-12d3-a456-426614174001\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "At least one supplier is required")
        List<UUID> supplierIds

) {
}