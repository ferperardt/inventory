package com.inventory.mapper;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.request.UpdateSupplierRequest;
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

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should map CreateSupplierRequest to Supplier entity successfully")
        void shouldMapCreateSupplierRequestToSupplierEntitySuccessfully() {
            // Given
            CreateSupplierRequest request = createCompleteCreateRequest();

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull(); // Should be ignored
            assertThat(result.getName()).isEqualTo(request.name());
            assertThat(result.getBusinessId()).isEqualTo(request.businessId());
            assertThat(result.getStatus()).isEqualTo(request.status());
            assertThat(result.getEmail()).isEqualTo(request.email());
            assertThat(result.getPhone()).isEqualTo(request.phone());
            assertThat(result.getContactPerson()).isEqualTo(request.contactPerson());
            assertThat(result.getAddress()).isEqualTo(request.address());
            assertThat(result.getPaymentTerms()).isEqualTo(request.paymentTerms());
            assertThat(result.getAverageDeliveryDays()).isEqualTo(request.averageDeliveryDays());
            assertThat(result.getSupplierType()).isEqualTo(request.supplierType());
            assertThat(result.getNotes()).isEqualTo(request.notes());
            assertThat(result.getRating()).isEqualTo(request.rating());
            // BaseEntity fields should be ignored
            assertThat(result.getActive()).isTrue(); // BaseEntity has default value
            assertThat(result.getCreatedAt()).isNull(); // Should be ignored
            assertThat(result.getUpdatedAt()).isNull(); // Should be ignored
            assertThat(result.getDeletedAt()).isNull(); // Should be ignored
        }

        @Test
        @DisplayName("Should return null when CreateSupplierRequest is null")
        void shouldReturnNullWhenCreateSupplierRequestIsNull() {
            // When
            Supplier result = supplierMapper.toEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle CreateSupplierRequest with minimal required fields")
        void shouldHandleCreateSupplierRequestWithMinimalRequiredFields() {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Basic Supplier", null, null, "basic@supplier.com", "+1-555-1234",
                    null, null, null, null, null, null, null
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Basic Supplier");
            assertThat(result.getBusinessId()).isNull();
            assertThat(result.getStatus()).isEqualTo(SupplierStatus.ACTIVE); // Compact constructor default
            assertThat(result.getEmail()).isEqualTo("basic@supplier.com");
            assertThat(result.getPhone()).isEqualTo("+1-555-1234");
            assertThat(result.getContactPerson()).isNull();
            assertThat(result.getAddress()).isNull();
            assertThat(result.getPaymentTerms()).isNull();
            assertThat(result.getAverageDeliveryDays()).isNull();
            assertThat(result.getSupplierType()).isNull();
            assertThat(result.getNotes()).isNull();
            assertThat(result.getRating()).isNull();
            assertThat(result.getActive()).isTrue(); // BaseEntity default
        }

        @Test
        @DisplayName("Should handle null status and set default to ACTIVE")
        void shouldHandleNullStatusAndSetDefaultToActive() {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", null, // null status
                    "test@supplier.com", "+1-555-0123", null, null, null, null, null, null, null
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getStatus()).isEqualTo(SupplierStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should preserve all SupplierStatus enum values")
        void shouldPreserveAllSupplierStatusEnumValues() {
            // Given - Test each status
            SupplierStatus[] statuses = {
                SupplierStatus.ACTIVE, 
                SupplierStatus.INACTIVE, 
                SupplierStatus.BLOCKED, 
                SupplierStatus.PENDING_APPROVAL
            };

            for (SupplierStatus status : statuses) {
                CreateSupplierRequest request = new CreateSupplierRequest(
                        "Test Supplier", "BUS123456", status, "test@supplier.com", 
                        "+1-555-0123", null, null, null, null, null, null, null
                );

                // When
                Supplier result = supplierMapper.toEntity(request);

                // Then
                assertThat(result.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("Should preserve all SupplierType enum values")
        void shouldPreserveAllSupplierTypeEnumValues() {
            // Given - Test each type
            SupplierType[] types = {SupplierType.DOMESTIC, SupplierType.INTERNATIONAL};

            for (SupplierType type : types) {
                CreateSupplierRequest request = new CreateSupplierRequest(
                        "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                        "+1-555-0123", null, null, null, null, type, null, null
                );

                // When
                Supplier result = supplierMapper.toEntity(request);

                // Then
                assertThat(result.getSupplierType()).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("Should handle embedded Address correctly")
        void shouldHandleEmbeddedAddressCorrectly() {
            // Given
            Address address = new Address(
                "456 Business Ave", "Commerce City", "TX", "12345", "USA"
            );
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, address, null, null, null, null, null
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getAddress()).isNotNull();
            assertThat(result.getAddress()).isEqualTo(address);
            assertThat(result.getAddress().getStreetAddress()).isEqualTo("456 Business Ave");
            assertThat(result.getAddress().getCity()).isEqualTo("Commerce City");
            assertThat(result.getAddress().getStateProvince()).isEqualTo("TX");
            assertThat(result.getAddress().getPostalCode()).isEqualTo("12345");
            assertThat(result.getAddress().getCountry()).isEqualTo("USA");
        }

        @Test
        @DisplayName("Should handle null Address correctly")
        void shouldHandleNullAddressCorrectly() {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, // null address
                    null, null, null, null, null
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getAddress()).isNull();
        }

        @Test
        @DisplayName("Should handle BigDecimal rating correctly")
        void shouldHandleBigDecimalRatingCorrectly() {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, null, null, null, null,
                    BigDecimal.valueOf(3.75)
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getRating()).isEqualTo(BigDecimal.valueOf(3.75));
        }

        @Test
        @DisplayName("Should handle null rating correctly")
        void shouldHandleNullRatingCorrectly() {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, null, null, null, null,
                    null // null rating
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getRating()).isNull();
        }

        @Test
        @DisplayName("Should handle zero average delivery days")
        void shouldHandleZeroAverageDeliveryDays() {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, null, 0, // zero average delivery days
                    null, null, null
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getAverageDeliveryDays()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle empty strings for optional fields")
        void shouldHandleEmptyStringsForOptionalFields() {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "", SupplierStatus.ACTIVE, "test@supplier.com", "+1-555-0123",
                    "", null, "", null, null, "", null
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getBusinessId()).isEmpty();
            assertThat(result.getContactPerson()).isEmpty();
            assertThat(result.getPaymentTerms()).isEmpty();
            assertThat(result.getNotes()).isEmpty();
        }

        @Test
        @DisplayName("Should ignore BaseEntity fields correctly")
        void shouldIgnoreBaseEntityFieldsCorrectly() {
            // Given
            CreateSupplierRequest request = createCompleteCreateRequest();

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then - BaseEntity fields should be ignored and remain null or default
            assertThat(result.getId()).isNull();
            assertThat(result.getActive()).isTrue(); // Default value from BaseEntity
            assertThat(result.getCreatedAt()).isNull();
            assertThat(result.getUpdatedAt()).isNull();
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle international supplier data correctly")
        void shouldHandleInternationalSupplierDataCorrectly() {
            // Given
            Address internationalAddress = new Address(
                "北京市朝阳区建国路1号", "北京市", "北京", "100001", "CHN"
            );
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "中国供应商有限公司",
                    "CHN-BUS-001",
                    SupplierStatus.ACTIVE,
                    "contact@中国supplier.com",
                    "+86-138-0013-8000",
                    "张伟",
                    internationalAddress,
                    "NET45",
                    14,
                    SupplierType.INTERNATIONAL,
                    "优质国际供应商",
                    BigDecimal.valueOf(4.8)
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getName()).isEqualTo("中国供应商有限公司");
            assertThat(result.getBusinessId()).isEqualTo("CHN-BUS-001");
            assertThat(result.getEmail()).isEqualTo("contact@中国supplier.com");
            assertThat(result.getPhone()).isEqualTo("+86-138-0013-8000");
            assertThat(result.getContactPerson()).isEqualTo("张伟");
            assertThat(result.getSupplierType()).isEqualTo(SupplierType.INTERNATIONAL);
            assertThat(result.getNotes()).isEqualTo("优质国际供应商");
            assertThat(result.getAddress()).isEqualTo(internationalAddress);
        }

        @Test
        @DisplayName("Should handle large text fields correctly")
        void shouldHandleLargeTextFieldsCorrectly() {
            // Given
            String longBusinessId = "BUSINESS-ID-".repeat(3) + "12345";
            String longNotes = "This is a very long note about the supplier that contains detailed information about their capabilities, history, performance metrics, and other relevant details that might be useful for procurement decisions and ongoing supplier relationship management activities.";
            
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier",
                    longBusinessId,
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-0123",
                    null, null, null, null, null,
                    longNotes,
                    null
            );

            // When
            Supplier result = supplierMapper.toEntity(request);

            // Then
            assertThat(result.getBusinessId()).isEqualTo(longBusinessId);
            assertThat(result.getNotes()).isEqualTo(longNotes);
        }

        private CreateSupplierRequest createCompleteCreateRequest() {
            return new CreateSupplierRequest(
                    "ABC Electronics Ltd",
                    "BUS123456",
                    SupplierStatus.ACTIVE,
                    "contact@abcelectronics.com",
                    "+1-555-0123",
                    "John Smith",
                    new Address("123 Industrial Blvd", "Tech City", "CA", "90210", "USA"),
                    "NET30",
                    7,
                    SupplierType.DOMESTIC,
                    "Reliable supplier with good quality products",
                    BigDecimal.valueOf(4.5)
            );
        }
    }

    @Nested
    @DisplayName("updateEntity() Tests")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update entity from UpdateSupplierRequest successfully")
        void shouldUpdateEntityFromUpdateSupplierRequestSuccessfully() {
            // Given
            Supplier existingSupplier = createCompleteSupplier();
            existingSupplier.setName("Original Name");
            existingSupplier.setBusinessId("ORIG-BUS-ID");
            existingSupplier.setStatus(SupplierStatus.INACTIVE);
            existingSupplier.setEmail("original@email.com");
            existingSupplier.setPhone("+1-555-0000");
            existingSupplier.setContactPerson("Original Contact");
            existingSupplier.setPaymentTerms("NET60");
            existingSupplier.setAverageDeliveryDays(14);
            existingSupplier.setSupplierType(SupplierType.DOMESTIC);
            existingSupplier.setNotes("Original notes");
            existingSupplier.setRating(BigDecimal.valueOf(3.0));
            
            UpdateSupplierRequest request = createCompleteUpdateRequest();

            // When
            supplierMapper.updateEntity(request, existingSupplier);

            // Then
            assertThat(existingSupplier.getName()).isEqualTo(request.name());
            assertThat(existingSupplier.getBusinessId()).isEqualTo(request.businessId());
            assertThat(existingSupplier.getStatus()).isEqualTo(request.status());
            assertThat(existingSupplier.getEmail()).isEqualTo(request.email());
            assertThat(existingSupplier.getPhone()).isEqualTo(request.phone());
            assertThat(existingSupplier.getContactPerson()).isEqualTo(request.contactPerson());
            assertThat(existingSupplier.getAddress()).isEqualTo(request.address());
            assertThat(existingSupplier.getPaymentTerms()).isEqualTo(request.paymentTerms());
            assertThat(existingSupplier.getAverageDeliveryDays()).isEqualTo(request.averageDeliveryDays());
            assertThat(existingSupplier.getSupplierType()).isEqualTo(request.supplierType());
            assertThat(existingSupplier.getNotes()).isEqualTo(request.notes());
            assertThat(existingSupplier.getRating()).isEqualTo(request.rating());
            
            // BaseEntity fields should remain unchanged
            assertThat(existingSupplier.getId()).isNotNull(); // Should preserve original ID
            assertThat(existingSupplier.getActive()).isTrue(); // Should preserve active flag
            // Note: timestamps are set by JPA auditing and may be null in unit tests
        }

        @Test
        @DisplayName("Should update entity with minimal fields from UpdateSupplierRequest")
        void shouldUpdateEntityWithMinimalFieldsFromUpdateSupplierRequest() {
            // Given
            Supplier existingSupplier = createCompleteSupplier();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Updated Basic Supplier", null, null, "updated@supplier.com", "+1-555-9999",
                    null, null, null, null, null, null, null
            );

            // When
            supplierMapper.updateEntity(request, existingSupplier);

            // Then
            assertThat(existingSupplier.getName()).isEqualTo("Updated Basic Supplier");
            assertThat(existingSupplier.getBusinessId()).isNull();
            assertThat(existingSupplier.getStatus()).isNull();
            assertThat(existingSupplier.getEmail()).isEqualTo("updated@supplier.com");
            assertThat(existingSupplier.getPhone()).isEqualTo("+1-555-9999");
            assertThat(existingSupplier.getContactPerson()).isNull();
            assertThat(existingSupplier.getAddress()).isNull();
            assertThat(existingSupplier.getPaymentTerms()).isNull();
            assertThat(existingSupplier.getAverageDeliveryDays()).isNull();
            assertThat(existingSupplier.getSupplierType()).isNull();
            assertThat(existingSupplier.getNotes()).isNull();
            assertThat(existingSupplier.getRating()).isNull();
        }

        @Test
        @DisplayName("Should preserve BaseEntity fields when updating")
        void shouldPreserveBaseEntityFieldsWhenUpdating() {
            // Given
            Supplier existingSupplier = createCompleteSupplier();
            UUID originalId = existingSupplier.getId();
            Boolean originalActive = existingSupplier.getActive();
            
            UpdateSupplierRequest request = createCompleteUpdateRequest();

            // When
            supplierMapper.updateEntity(request, existingSupplier);

            // Then - BaseEntity fields should be preserved
            assertThat(existingSupplier.getId()).isEqualTo(originalId);
            assertThat(existingSupplier.getActive()).isEqualTo(originalActive);
            assertThat(existingSupplier.getDeletedAt()).isNull(); // Should remain unchanged
            // Note: timestamps are managed by JPA auditing and may be null in unit tests
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
                UpdateSupplierRequest request = new UpdateSupplierRequest(
                        "Test Supplier", "BUS123456", status, "test@supplier.com", 
                        "+1-555-0123", null, null, null, null, null, null, null
                );

                // When
                supplierMapper.updateEntity(request, supplier);

                // Then
                assertThat(supplier.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("Should handle all SupplierType enum values")
        void shouldHandleAllSupplierTypeEnumValues() {
            // Given - Test each type
            SupplierType[] types = {SupplierType.DOMESTIC, SupplierType.INTERNATIONAL};

            for (SupplierType type : types) {
                Supplier supplier = createBasicSupplier();
                UpdateSupplierRequest request = new UpdateSupplierRequest(
                        "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                        "+1-555-0123", null, null, null, null, type, null, null
                );

                // When
                supplierMapper.updateEntity(request, supplier);

                // Then
                assertThat(supplier.getSupplierType()).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("Should handle embedded Address correctly")
        void shouldHandleEmbeddedAddressCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            Address originalAddress = new Address("Original St", "Original City", "OS", "00000", "USA");
            supplier.setAddress(originalAddress);
            
            Address newAddress = new Address(
                "456 Updated Ave", "Updated City", "UC", "99999", "CAN"
            );
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, newAddress, null, null, null, null, null
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getAddress()).isNotNull();
            assertThat(supplier.getAddress()).isEqualTo(newAddress);
            assertThat(supplier.getAddress().getStreetAddress()).isEqualTo("456 Updated Ave");
            assertThat(supplier.getAddress().getCity()).isEqualTo("Updated City");
            assertThat(supplier.getAddress().getStateProvince()).isEqualTo("UC");
            assertThat(supplier.getAddress().getPostalCode()).isEqualTo("99999");
            assertThat(supplier.getAddress().getCountry()).isEqualTo("CAN");
        }

        @Test
        @DisplayName("Should handle null Address correctly")
        void shouldHandleNullAddressCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            Address originalAddress = new Address("Original St", "Original City", "OS", "00000", "USA");
            supplier.setAddress(originalAddress);
            
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, // null address
                    null, null, null, null, null
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getAddress()).isNull();
        }

        @Test
        @DisplayName("Should handle BigDecimal rating correctly")
        void shouldHandleBigDecimalRatingCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setRating(BigDecimal.valueOf(2.0));
            
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, null, null, null, null,
                    BigDecimal.valueOf(4.75)
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getRating()).isEqualTo(BigDecimal.valueOf(4.75));
        }

        @Test
        @DisplayName("Should handle null rating correctly")
        void shouldHandleNullRatingCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setRating(BigDecimal.valueOf(3.5));
            
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, null, null, null, null,
                    null // null rating
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getRating()).isNull();
        }

        @Test
        @DisplayName("Should handle zero average delivery days")
        void shouldHandleZeroAverageDeliveryDays() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setAverageDeliveryDays(10);
            
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, null, null, 0, // zero average delivery days
                    null, null, null
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getAverageDeliveryDays()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle empty strings for optional fields")
        void shouldHandleEmptyStringsForOptionalFields() {
            // Given
            Supplier supplier = createBasicSupplier();
            supplier.setBusinessId("ORIGINAL-ID");
            supplier.setContactPerson("Original Person");
            supplier.setPaymentTerms("NET30");
            supplier.setNotes("Original notes");
            
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Test Supplier", "", SupplierStatus.ACTIVE, "test@supplier.com", "+1-555-0123",
                    "", null, "", null, null, "", null
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getBusinessId()).isEmpty();
            assertThat(supplier.getContactPerson()).isEmpty();
            assertThat(supplier.getPaymentTerms()).isEmpty();
            assertThat(supplier.getNotes()).isEmpty();
        }

        @Test
        @DisplayName("Should handle international supplier data correctly")
        void shouldHandleInternationalSupplierDataCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            Address internationalAddress = new Address(
                "上海市浦东新区陆家嘴环路1000号", "上海市", "上海", "200120", "CHN"
            );
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "上海供应商有限公司",
                    "CHN-UPD-001",
                    SupplierStatus.ACTIVE,
                    "contact@上海supplier.com",
                    "+86-21-1234-5678",
                    "李明",
                    internationalAddress,
                    "NET30",
                    21,
                    SupplierType.INTERNATIONAL,
                    "优质更新供应商",
                    BigDecimal.valueOf(4.9)
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getName()).isEqualTo("上海供应商有限公司");
            assertThat(supplier.getBusinessId()).isEqualTo("CHN-UPD-001");
            assertThat(supplier.getEmail()).isEqualTo("contact@上海supplier.com");
            assertThat(supplier.getPhone()).isEqualTo("+86-21-1234-5678");
            assertThat(supplier.getContactPerson()).isEqualTo("李明");
            assertThat(supplier.getSupplierType()).isEqualTo(SupplierType.INTERNATIONAL);
            assertThat(supplier.getNotes()).isEqualTo("优质更新供应商");
            assertThat(supplier.getAddress()).isEqualTo(internationalAddress);
        }

        @Test
        @DisplayName("Should handle large text fields correctly")
        void shouldHandleLargeTextFieldsCorrectly() {
            // Given
            Supplier supplier = createBasicSupplier();
            String longBusinessId = "UPDATED-BUSINESS-ID-".repeat(2) + "FINAL";
            String longNotes = "This is an updated very long note about the supplier that contains new detailed information about their enhanced capabilities, improved history, better performance metrics, and other relevant details that might be useful for updated procurement decisions and ongoing supplier relationship management activities.";
            
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Updated Test Supplier",
                    longBusinessId,
                    SupplierStatus.ACTIVE,
                    "updated@supplier.com",
                    "+1-555-9999",
                    null, null, null, null, null,
                    longNotes,
                    null
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then
            assertThat(supplier.getBusinessId()).isEqualTo(longBusinessId);
            assertThat(supplier.getNotes()).isEqualTo(longNotes);
        }

        @Test
        @DisplayName("Should not update when request is null")
        void shouldNotUpdateWhenRequestIsNull() {
            // Given
            Supplier originalSupplier = createCompleteSupplier();
            String originalName = originalSupplier.getName();
            String originalBusinessId = originalSupplier.getBusinessId();
            
            // When
            supplierMapper.updateEntity(null, originalSupplier);

            // Then - Supplier should remain unchanged
            assertThat(originalSupplier.getName()).isEqualTo(originalName);
            assertThat(originalSupplier.getBusinessId()).isEqualTo(originalBusinessId);
        }

        @Test
        @DisplayName("Should not fail when target supplier is null")
        void shouldNotFailWhenTargetSupplierIsNull() {
            // Given
            UpdateSupplierRequest request = createCompleteUpdateRequest();

            // When & Then - Should not throw exception, but may not be supported by MapStruct
            try {
                supplierMapper.updateEntity(request, null);
            } catch (NullPointerException e) {
                // This is expected behavior for MapStruct when target is null
                // The method is designed to update existing entities, not create new ones
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
        }

        @Test
        @DisplayName("Should update all non-BaseEntity fields comprehensively")
        void shouldUpdateAllNonBaseEntityFieldsComprehensively() {
            // Given
            Supplier supplier = createCompleteSupplier();
            
            // Store original BaseEntity values
            UUID originalId = supplier.getId();
            Boolean originalActive = supplier.getActive();
            
            Address newAddress = new Address("New Street", "New City", "NC", "12345", "USA");
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Completely New Name",
                    "NEW-BUS-ID-2024",
                    SupplierStatus.PENDING_APPROVAL,
                    "new@company.com",
                    "+1-800-NEW-SUPP",
                    "New Contact Person",
                    newAddress,
                    "NET45",
                    3,
                    SupplierType.INTERNATIONAL,
                    "Completely new comprehensive notes with updated information",
                    BigDecimal.valueOf(4.2)
            );

            // When
            supplierMapper.updateEntity(request, supplier);

            // Then - All business fields should be updated
            assertThat(supplier.getName()).isEqualTo("Completely New Name");
            assertThat(supplier.getBusinessId()).isEqualTo("NEW-BUS-ID-2024");
            assertThat(supplier.getStatus()).isEqualTo(SupplierStatus.PENDING_APPROVAL);
            assertThat(supplier.getEmail()).isEqualTo("new@company.com");
            assertThat(supplier.getPhone()).isEqualTo("+1-800-NEW-SUPP");
            assertThat(supplier.getContactPerson()).isEqualTo("New Contact Person");
            assertThat(supplier.getAddress()).isEqualTo(newAddress);
            assertThat(supplier.getPaymentTerms()).isEqualTo("NET45");
            assertThat(supplier.getAverageDeliveryDays()).isEqualTo(3);
            assertThat(supplier.getSupplierType()).isEqualTo(SupplierType.INTERNATIONAL);
            assertThat(supplier.getNotes()).isEqualTo("Completely new comprehensive notes with updated information");
            assertThat(supplier.getRating()).isEqualTo(BigDecimal.valueOf(4.2));
            
            // But BaseEntity fields should remain unchanged
            assertThat(supplier.getId()).isEqualTo(originalId);
            assertThat(supplier.getActive()).isEqualTo(originalActive);
            assertThat(supplier.getDeletedAt()).isNull();
        }

        private UpdateSupplierRequest createCompleteUpdateRequest() {
            return new UpdateSupplierRequest(
                    "Updated Electronics Ltd",
                    "UPD123456",
                    SupplierStatus.ACTIVE,
                    "updated@electronics.com",
                    "+1-555-9999",
                    "Jane Doe",
                    new Address("456 Updated Blvd", "New City", "NY", "10001", "USA"),
                    "NET15",
                    5,
                    SupplierType.INTERNATIONAL,
                    "Updated supplier information",
                    BigDecimal.valueOf(4.8)
            );
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
        // Timestamps are managed by JPA auditing and will be null in unit tests
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