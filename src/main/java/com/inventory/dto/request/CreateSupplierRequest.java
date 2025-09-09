package com.inventory.dto.request;

import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateSupplierRequest(

        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Size(max = 50, message = "Business ID must not exceed 50 characters")
        String businessId,

        SupplierStatus status,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @Size(max = 100, message = "Contact person must not exceed 100 characters")
        String contactPerson,

        @Valid
        Address address,

        @Size(max = 100, message = "Payment terms must not exceed 100 characters")
        String paymentTerms,

        @Min(value = 1, message = "Average delivery days must be at least 1")
        Integer averageDeliveryDays,

        SupplierType supplierType,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

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