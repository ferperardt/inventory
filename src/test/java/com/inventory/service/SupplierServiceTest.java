package com.inventory.service;

import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Address;
import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.mapper.SupplierMapper;
import com.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierService Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierService(supplierRepository, supplierMapper);
    }

    @Nested
    @DisplayName("getAllSuppliers() Tests")
    class GetAllSuppliersTests {

        @Test
        @DisplayName("Should return paginated active suppliers")
        void shouldReturnPaginatedActiveSuppliers() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findByActiveTrue(pageable)).willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.getAllSuppliers(pageable);

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getNumber()).isEqualTo(0);
            then(supplierRepository).should().findByActiveTrue(pageable);
            then(supplierMapper).should().toResponse(supplier);
        }

        @Test
        @DisplayName("Should return empty page when no active suppliers exist")
        void shouldReturnEmptyPageWhenNoActiveSuppliersExist() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Supplier> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(supplierRepository.findByActiveTrue(pageable)).willReturn(emptyPage);

            // When
            Page<SupplierResponse> result = supplierService.getAllSuppliers(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.isEmpty()).isTrue();
            then(supplierRepository).should().findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("Should handle multiple suppliers correctly")
        void shouldHandleMultipleSuppliersCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Supplier supplier1 = createSupplier("ABC Electronics Ltd");
            Supplier supplier2 = createSupplier("XYZ Components Inc");
            SupplierResponse response1 = createSupplierResponse("ABC Electronics Ltd");
            SupplierResponse response2 = createSupplierResponse("XYZ Components Inc");
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier1, supplier2), pageable, 2);

            given(supplierRepository.findByActiveTrue(pageable)).willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier1)).willReturn(response1);
            given(supplierMapper.toResponse(supplier2)).willReturn(response2);

            // When
            Page<SupplierResponse> result = supplierService.getAllSuppliers(pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(response1, response2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            then(supplierRepository).should().findByActiveTrue(pageable);
            then(supplierMapper).should().toResponse(supplier1);
            then(supplierMapper).should().toResponse(supplier2);
        }

        @Test
        @DisplayName("Should handle different page sizes correctly")
        void shouldHandleDifferentPageSizesCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(1, 5);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 10);

            given(supplierRepository.findByActiveTrue(pageable)).willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.getAllSuppliers(pageable);

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getNumber()).isEqualTo(1);
            then(supplierRepository).should().findByActiveTrue(pageable);
            then(supplierMapper).should().toResponse(supplier);
        }

        @Test
        @DisplayName("Should preserve page information from repository response")
        void shouldPreservePageInformationFromRepositoryResponse() {
            // Given
            Pageable pageable = PageRequest.of(2, 15);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 50);

            given(supplierRepository.findByActiveTrue(pageable)).willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.getAllSuppliers(pageable);

            // Then
            assertThat(result.getTotalElements()).isEqualTo(50);
            assertThat(result.getTotalPages()).isEqualTo(4);
            assertThat(result.getSize()).isEqualTo(15);
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isFalse();
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isTrue();
            then(supplierRepository).should().findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("Should call mapper for each supplier in the page")
        void shouldCallMapperForEachSupplierInThePage() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier1 = createSupplier("Supplier A");
            Supplier supplier2 = createSupplier("Supplier B");
            Supplier supplier3 = createSupplier("Supplier C");
            List<Supplier> suppliers = List.of(supplier1, supplier2, supplier3);
            Page<Supplier> supplierPage = new PageImpl<>(suppliers, pageable, 3);

            SupplierResponse response1 = createSupplierResponse("Supplier A");
            SupplierResponse response2 = createSupplierResponse("Supplier B");
            SupplierResponse response3 = createSupplierResponse("Supplier C");

            given(supplierRepository.findByActiveTrue(pageable)).willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier1)).willReturn(response1);
            given(supplierMapper.toResponse(supplier2)).willReturn(response2);
            given(supplierMapper.toResponse(supplier3)).willReturn(response3);

            // When
            Page<SupplierResponse> result = supplierService.getAllSuppliers(pageable);

            // Then
            assertThat(result.getContent()).hasSize(3);
            then(supplierMapper).should().toResponse(supplier1);
            then(supplierMapper).should().toResponse(supplier2);
            then(supplierMapper).should().toResponse(supplier3);
        }
    }

    private Supplier createSupplier() {
        return createSupplier("ABC Electronics Ltd");
    }

    private Supplier createSupplier(String name) {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName(name);
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
        return supplier;
    }

    private SupplierResponse createSupplierResponse() {
        return createSupplierResponse("ABC Electronics Ltd");
    }

    private SupplierResponse createSupplierResponse(String name) {
        return new SupplierResponse(
                UUID.randomUUID(),
                name,
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
                BigDecimal.valueOf(4.5),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}