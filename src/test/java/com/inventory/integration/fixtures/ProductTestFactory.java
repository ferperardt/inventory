package com.inventory.integration.fixtures;

import com.inventory.dto.request.CreateProductRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductTestFactory {

    public static CreateProductRequest validProductRequest(UUID supplierId) {
        return new CreateProductRequest(
                "Test Product " + System.currentTimeMillis(),
                "Test product description for integration tests",
                "TEST-" + System.currentTimeMillis(),
                BigDecimal.valueOf(29.99),
                10,
                5,
                "Electronics",
                List.of(supplierId)
        );
    }

    public static CreateProductRequest validProductRequest(List<UUID> supplierIds) {
        return new CreateProductRequest(
                "Test Product " + System.currentTimeMillis(),
                "Test product description for integration tests",
                "TEST-" + System.currentTimeMillis(),
                BigDecimal.valueOf(29.99),
                10,
                5,
                "Electronics",
                supplierIds
        );
    }

    public static CreateProductRequest invalidProductRequest() {
        return new CreateProductRequest(
                "", // Invalid: empty name
                null,
                "invalid-sku-lowercase", // Invalid: lowercase
                BigDecimal.valueOf(-1), // Invalid: negative price
                -5, // Invalid: negative stock
                -1, // Invalid: negative min stock
                "Very long category name that exceeds the maximum allowed length of 50 characters", // Invalid: too long
                null // Invalid: null suppliers
        );
    }

    public static CreateProductRequest duplicateSkuProductRequest(String existingSku, UUID supplierId) {
        return new CreateProductRequest(
                "Duplicate SKU Product",
                "Product with existing SKU",
                existingSku, // This should cause conflict
                BigDecimal.valueOf(19.99),
                5,
                2,
                "Test",
                List.of(supplierId)
        );
    }

    public static CreateProductRequest invalidStockLevelProductRequest(UUID supplierId) {
        return new CreateProductRequest(
                "Invalid Stock Product",
                "Product with stock below min level",
                "INVALID-STOCK-" + System.currentTimeMillis(),
                BigDecimal.valueOf(19.99),
                3, // Stock quantity
                10, // Min stock level (stock < minStock should fail)
                "Test",
                List.of(supplierId)
        );
    }

    public static CreateProductRequest productWithNonExistentSupplier() {
        return new CreateProductRequest(
                "Product with Invalid Supplier",
                "This product references a non-existent supplier",
                "INVALID-SUPPLIER-" + System.currentTimeMillis(),
                BigDecimal.valueOf(39.99),
                8,
                3,
                "Test",
                List.of(UUID.randomUUID()) // Random UUID that doesn't exist
        );
    }

    public static CreateProductRequest customProductRequest(String name, String sku, UUID supplierId) {
        return new CreateProductRequest(
                name,
                "Test product for " + name,
                sku,
                BigDecimal.valueOf(29.99),
                10,
                5,
                "Test",
                List.of(supplierId)
        );
    }
}