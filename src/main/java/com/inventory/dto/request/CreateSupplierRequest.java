package com.inventory.dto.request;

import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to create a new supplier")
public record CreateSupplierRequest(

        @Schema(description = "Supplier company name", example = "TechCorp Suppliers Ltd", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Schema(description = "Business registration ID or tax number", example = "TC2024001")
        @Size(max = 50, message = "Business ID must not exceed 50 characters")
        String businessId,

        @Schema(description = "Supplier status", example = "ACTIVE", defaultValue = "ACTIVE")
        SupplierStatus status,

        @Schema(description = "Contact email address", example = "contact@techcorp.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Schema(description = "Contact phone number", example = "+1-555-0123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Schema(description = "Primary contact person name", example = "John Smith")
        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        @Schema(description = "Supplier address information")
        @Valid
        Address address,

        @Schema(description = "Payment terms and conditions", example = "Net 30 days")
        @Size(max = 100, message = "Payment terms must not exceed 100 characters")
        String paymentTerms,

        @Schema(description = "Average delivery time in days", example = "7")
        @Min(value = 1, message = "Average delivery days must be at least 1")
        Integer averageDeliveryDays,

        @Schema(description = "Type of supplier business", example = "DOMESTIC")
        SupplierType supplierType,

        @Schema(description = "Additional notes about the supplier", example = "Preferred supplier for electronic components")
        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

        @Schema(description = "Supplier performance rating", example = "4.5", minimum = "1.0", maximum = "5.0")
        @DecimalMin(value = "1.0", message = "Rating must be between 1.0 and 5.0")
        @DecimalMax(value = "5.0", message = "Rating must be between 1.0 and 5.0")
        @Digits(integer = 1, fraction = 2, message = "Rating must have at most 1 integer digit and 2 decimal places")
        BigDecimal rating

) {
    public CreateSupplierRequest {
        if (status == null) {
            status = SupplierStatus.ACTIVE;
        }
    }
}