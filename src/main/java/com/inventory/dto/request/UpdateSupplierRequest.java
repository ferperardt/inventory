package com.inventory.dto.request;

import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to update supplier information")
public record UpdateSupplierRequest(

        @Schema(description = "Updated supplier name", example = "TechCorp Suppliers International", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Schema(description = "Updated business ID", example = "TC2024002")
        @Size(max = 50, message = "Business ID must not exceed 50 characters")
        String businessId,

        @Schema(description = "Updated supplier status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Status is required")
        SupplierStatus status,

        @Schema(description = "Updated email address", example = "contact@techcorp-intl.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Schema(description = "Updated phone number", example = "+1-555-0199", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Schema(description = "Updated contact person", example = "Jane Doe")
        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        @Schema(description = "Updated address information")
        @Valid
        Address address,

        @Schema(description = "Updated payment terms", example = "Net 45 days")
        @Size(max = 100, message = "Payment terms must not exceed 100 characters")
        String paymentTerms,

        @Schema(description = "Updated delivery time", example = "5")
        @Min(value = 1, message = "Average delivery days must be at least 1")
        Integer averageDeliveryDays,

        @Schema(description = "Updated supplier type", example = "INTERNATIONAL")
        SupplierType supplierType,

        @Schema(description = "Updated notes", example = "Now handling distribution for multiple regions")
        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

        @Schema(description = "Updated performance rating", example = "4.8", minimum = "1.0", maximum = "5.0")
        @DecimalMin(value = "1.0", message = "Rating must be between 1.0 and 5.0")
        @DecimalMax(value = "5.0", message = "Rating must be between 1.0 and 5.0")
        @Digits(integer = 1, fraction = 2, message = "Rating must have at most 1 integer digit and 2 decimal places")
        BigDecimal rating

) {
}