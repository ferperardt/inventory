package com.inventory.dto.response;

import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Supplier information with business details and performance metrics")
public record SupplierResponse(
        @Schema(description = "Supplier unique identifier", example = "456e1234-e89b-12d3-a456-426614174001")
        UUID id,
        @Schema(description = "Supplier company name", example = "TechCorp Suppliers Ltd")
        String name,
        @Schema(description = "Business registration ID", example = "TC2024001")
        String businessId,
        @Schema(description = "Current supplier status", example = "ACTIVE")
        SupplierStatus status,
        @Schema(description = "Contact email address", example = "contact@techcorp.com")
        String email,
        @Schema(description = "Contact phone number", example = "+1-555-0123")
        String phone,
        @Schema(description = "Primary contact person", example = "John Smith")
        String contactPerson,
        @Schema(description = "Supplier address information")
        Address address,
        @Schema(description = "Payment terms", example = "Net 30 days")
        String paymentTerms,
        @Schema(description = "Average delivery time in days", example = "7")
        Integer averageDeliveryDays,
        @Schema(description = "Type of supplier business", example = "DOMESTIC")
        SupplierType supplierType,
        @Schema(description = "Additional notes", example = "Preferred supplier for electronic components")
        String notes,
        @Schema(description = "Performance rating", example = "4.5")
        BigDecimal rating,
        @Schema(description = "Active status", example = "true")
        Boolean active,
        @Schema(description = "Creation timestamp", example = "2024-01-10T09:15:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update timestamp", example = "2024-01-25T16:20:00")
        LocalDateTime updatedAt
) {
}