package com.inventory.integration.fixtures;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.entity.Address;
import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;

import java.math.BigDecimal;

public class SupplierTestFactory {

    public static CreateSupplierRequest validSupplierRequest() {
        return new CreateSupplierRequest(
                "Test Supplier " + System.currentTimeMillis(),
                "TEST-" + System.currentTimeMillis(),
                SupplierStatus.ACTIVE,
                "test" + System.currentTimeMillis() + "@supplier.com",
                "+1-555-0199",
                "John Test",
                new Address(
                        "123 Test St",
                        "Test City",
                        "TC",
                        "12345",
                        "USA"
                ),
                "NET30",
                7,
                SupplierType.DOMESTIC,
                "Test supplier for integration tests",
                new BigDecimal("4.5")
        );
    }

    public static Supplier validSupplierEntity() {
        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier " + System.currentTimeMillis());
        supplier.setBusinessId("TEST-" + System.currentTimeMillis());
        supplier.setEmail("test" + System.currentTimeMillis() + "@supplier.com");
        supplier.setPhone("+1-555-0199");
        supplier.setContactPerson("John Test");
        supplier.setAddress(new Address("123 Test St", "Test City", "TC", "12345", "USA"));
        supplier.setPaymentTerms("NET30");
        supplier.setAverageDeliveryDays(7);
        supplier.setSupplierType(SupplierType.DOMESTIC);
        supplier.setNotes("Test supplier for integration tests");
        supplier.setRating(new BigDecimal("4.5"));
        return supplier;
    }

    public static Supplier validSupplierEntity(String name) {
        Supplier supplier = validSupplierEntity();
        supplier.setName(name);
        return supplier;
    }
}