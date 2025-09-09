package com.inventory.mapper;

import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Address;
import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SupplierMapper Tests")
class SupplierMapperTest {

    @Autowired
    private SupplierMapper supplierMapper;

    @Nested
    @DisplayName("toResponse() Tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should map Supplier to SupplierResponse successfully")
        void shouldMapSupplierToSupplierResponseSuccessfully() {
            // Given
            Supplier supplier = createCompleteSupplier();

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(supplier.getId());
            assertThat(result.name()).isEqualTo(supplier.getName());
            assertThat(result.businessId()).isEqualTo(supplier.getBusinessId());
            assertThat(result.status()).isEqualTo(supplier.getStatus());
            assertThat(result.email()).isEqualTo(supplier.getEmail());
            assertThat(result.phone()).isEqualTo(supplier.getPhone());
            assertThat(result.contactPerson()).isEqualTo(supplier.getContactPerson());
            assertThat(result.address()).isEqualTo(supplier.getAddress());
            assertThat(result.paymentTerms()).isEqualTo(supplier.getPaymentTerms());
            assertThat(result.averageDeliveryDays()).isEqualTo(supplier.getAverageDeliveryDays());
            assertThat(result.supplierType()).isEqualTo(supplier.getSupplierType());
            assertThat(result.notes()).isEqualTo(supplier.getNotes());
            assertThat(result.rating()).isEqualTo(supplier.getRating());
            assertThat(result.active()).isEqualTo(supplier.getActive());
            assertThat(result.createdAt()).isEqualTo(supplier.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(supplier.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle Supplier with minimal required fields")
        void shouldHandleSupplierWithMinimalRequiredFields() {
            // Given
            Supplier supplier = new Supplier();
            supplier.setId(UUID.randomUUID());
            supplier.setName("Basic Supplier");
            supplier.setEmail("basic@supplier.com");
            supplier.setPhone("+1-555-1234");
            supplier.setStatus(SupplierStatus.ACTIVE);

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(supplier.getId());
            assertThat(result.name()).isEqualTo("Basic Supplier");
            assertThat(result.businessId()).isNull();
            assertThat(result.status()).isEqualTo(SupplierStatus.ACTIVE);
            assertThat(result.email()).isEqualTo("basic@supplier.com");
            assertThat(result.phone()).isEqualTo("+1-555-1234");
            assertThat(result.contactPerson()).isNull();
            assertThat(result.address()).isNull();
            assertThat(result.paymentTerms()).isNull();
            assertThat(result.averageDeliveryDays()).isNull();
            assertThat(result.supplierType()).isNull();
            assertThat(result.notes()).isNull();
            assertThat(result.rating()).isNull();
            assertThat(result.active()).isEqualTo(supplier.getActive());
            assertThat(result.createdAt()).isNull();
            assertThat(result.updatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle all SupplierStatus enum values")
        void shouldHandleAllSupplierStatusEnumValues() {
            // Given - Test each status
            SupplierStatus[] statuses = {
                SupplierStatus.ACTIVE, 
                SupplierStatus.INACTIVE, 
                SupplierStatus.BLOCKED, 
                SupplierStatus.PENDING_APPROVAL
            };

            for (SupplierStatus status : statuses) {
                Supplier supplier = createBasicSupplier();
                supplier.setStatus(status);

                // When
                SupplierResponse result = supplierMapper.toResponse(supplier);

                // Then
                assertThat(result.status()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("Should handle all SupplierType enum values")
        void shouldHandleAllSupplierTypeEnumValues() {
            // Given - Test each type
            SupplierType[] types = {SupplierType.DOMESTIC, SupplierType.INTERNATIONAL};

            for (SupplierType type : types) {
                Supplier supplier = createBasicSupplier();
                supplier.setSupplierType(type);

                // When
                SupplierResponse result = supplierMapper.toResponse(supplier);

                // Then
                assertThat(result.supplierType()).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("Should handle embedded Address correctly")
        void shouldHandleEmbeddedAddressCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            Address address = new Address(
                "456 Business Ave",
                "Commerce City", 
                "TX",
                "12345",
                "USA"
            );
            supplier.setAddress(address);

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.address()).isNotNull();
            assertThat(result.address().getStreetAddress()).isEqualTo("456 Business Ave");
            assertThat(result.address().getCity()).isEqualTo("Commerce City");
            assertThat(result.address().getStateProvince()).isEqualTo("TX");
            assertThat(result.address().getPostalCode()).isEqualTo("12345");
            assertThat(result.address().getCountry()).isEqualTo("USA");
        }

        @Test
        @DisplayName("Should handle null Address correctly")
        void shouldHandleNullAddressCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setAddress(null);

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.address()).isNull();
        }

        @Test
        @DisplayName("Should handle BigDecimal rating correctly")
        void shouldHandleBigDecimalRatingCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setRating(BigDecimal.valueOf(3.75));

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.rating()).isEqualTo(BigDecimal.valueOf(3.75));
        }

        @Test
        @DisplayName("Should handle null rating correctly")
        void shouldHandleNullRatingCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setRating(null);

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.rating()).isNull();
        }

        @Test
        @DisplayName("Should handle zero average delivery days")
        void shouldHandleZeroAverageDeliveryDays() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setAverageDeliveryDays(0);

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.averageDeliveryDays()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle empty strings for optional fields")
        void shouldHandleEmptyStringsForOptionalFields() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setBusinessId("");
            supplier.setContactPerson("");
            supplier.setPaymentTerms("");
            supplier.setNotes("");

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.businessId()).isEmpty();
            assertThat(result.contactPerson()).isEmpty();
            assertThat(result.paymentTerms()).isEmpty();
            assertThat(result.notes()).isEmpty();
        }

        @Test
        @DisplayName("Should preserve timestamps from BaseEntity")
        void shouldPreserveTimestampsFromBaseEntity() {
            // Given
            Supplier supplier = createCompleteSupplier(); // This has timestamps set

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.createdAt()).isEqualTo(supplier.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(supplier.getUpdatedAt());
        }

        @Test
        @DisplayName("Should preserve active flag from BaseEntity")
        void shouldPreserveActiveFlagFromBaseEntity() {
            // Given - Test with active supplier (default value)
            Supplier activeSupplier = createBasicSupplier();

            // When
            SupplierResponse activeResult = supplierMapper.toResponse(activeSupplier);

            // Then
            assertThat(activeResult.active()).isTrue();

            // Given - Test with soft deleted supplier (inactive)
            Supplier inactiveSupplier = createBasicSupplier();
            inactiveSupplier.softDelete(); // This sets active to false

            // When
            SupplierResponse inactiveResult = supplierMapper.toResponse(inactiveSupplier);

            // Then
            assertThat(inactiveResult.active()).isFalse();
        }

        @Test
        @DisplayName("Should return null when Supplier is null")
        void shouldReturnNullWhenSupplierIsNull() {
            // When
            SupplierResponse result = supplierMapper.toResponse(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle long text fields correctly")
        void shouldHandleLongTextFieldsCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            String longNotes = "This is a very long note about the supplier that contains detailed information about their capabilities, history, performance metrics, and other relevant details that might be useful for procurement decisions.";
            supplier.setNotes(longNotes);

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.notes()).isEqualTo(longNotes);
        }

        @Test
        @DisplayName("Should handle international supplier data correctly")
        void shouldHandleInternationalSupplierDataCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setName("中国供应商有限公司");
            supplier.setEmail("contact@中国supplier.com");
            supplier.setPhone("+86-138-0013-8000");
            supplier.setSupplierType(SupplierType.INTERNATIONAL);
            
            Address internationalAddress = new Address(
                "北京市朝阳区建国路1号",
                "北京市",
                "北京",
                "100001",
                "CHN"
            );
            supplier.setAddress(internationalAddress);

            // When
            SupplierResponse result = supplierMapper.toResponse(supplier);

            // Then
            assertThat(result.name()).isEqualTo("中国供应商有限公司");
            assertThat(result.email()).isEqualTo("contact@中国supplier.com");
            assertThat(result.phone()).isEqualTo("+86-138-0013-8000");
            assertThat(result.supplierType()).isEqualTo(SupplierType.INTERNATIONAL);
            assertThat(result.address().getStreetAddress()).isEqualTo("北京市朝阳区建国路1号");
            assertThat(result.address().getCity()).isEqualTo("北京市");
            assertThat(result.address().getStateProvince()).isEqualTo("北京");
            assertThat(result.address().getPostalCode()).isEqualTo("100001");
            assertThat(result.address().getCountry()).isEqualTo("CHN");
        }
    }

    private Supplier createCompleteSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("ABC Electronics Ltd");
        supplier.setBusinessId("BUS123456");
        supplier.setStatus(SupplierStatus.ACTIVE);
        supplier.setEmail("contact@abcelectronics.com");
        supplier.setPhone("+1-555-0123");
        supplier.setContactPerson("John Smith");
        supplier.setAddress(new Address("123 Industrial Blvd", "Tech City", "CA", "90210", "USA"));
        supplier.setPaymentTerms("NET30");
        supplier.setAverageDeliveryDays(7);
        supplier.setSupplierType(SupplierType.DOMESTIC);
        supplier.setNotes("Reliable supplier with good quality products");
        supplier.setRating(BigDecimal.valueOf(4.5));
        // Timestamps are set by JPA auditing, active defaults to true
        return supplier;
    }

    private Supplier createBasicSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Test Supplier");
        supplier.setEmail("test@supplier.com");
        supplier.setPhone("+1-555-9999");
        supplier.setStatus(SupplierStatus.ACTIVE);
        return supplier;
    }
}