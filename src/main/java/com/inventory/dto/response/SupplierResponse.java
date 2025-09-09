package com.inventory.dto.response;

import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String businessId,
        SupplierStatus status,
        String email,
        String phone,
        String contactPerson,
        Address address,
        String paymentTerms,
        Integer averageDeliveryDays,
        SupplierType supplierType,
        String notes,
        BigDecimal rating,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}