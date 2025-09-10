package com.inventory.service;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.request.UpdateSupplierRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Address;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.exception.DuplicateBusinessIdException;
import com.inventory.exception.SupplierNotFoundException;
import com.inventory.mapper.ProductMapper;
import com.inventory.mapper.SupplierMapper;
import com.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierService Tests")
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private ProductMapper productMapper;

    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierService(supplierRepository, supplierMapper, productMapper);
    }

    @Nested
    @DisplayName("createSupplier() Tests")
    class CreateSupplierTests {

        @Test
        @DisplayName("Should create supplier successfully")
        void shouldCreateSupplierSuccessfully() {
            // Given
            CreateSupplierRequest request = createCompleteCreateRequest();
            Supplier supplier = createSupplier();
            Supplier savedSupplier = createSupplier();
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.existsByBusinessIdAndActiveTrue(request.businessId())).willReturn(false);
            given(supplierMapper.toEntity(request)).willReturn(supplier);
            given(supplierRepository.save(supplier)).willReturn(savedSupplier);
            given(supplierMapper.toResponse(savedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.createSupplier(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should().existsByBusinessIdAndActiveTrue(request.businessId());
            then(supplierMapper).should().toEntity(request);
            then(supplierRepository).should().save(supplier);
            then(supplierMapper).should().toResponse(savedSupplier);
        }

        @Test
        @DisplayName("Should create supplier with null business ID successfully")
        void shouldCreateSupplierWithNullBusinessIdSuccessfully() {
            // Given
            CreateSupplierRequest request = createRequestWithNullBusinessId();
            Supplier supplier = createSupplier();
            Supplier savedSupplier = createSupplier();
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierMapper.toEntity(request)).willReturn(supplier);
            given(supplierRepository.save(supplier)).willReturn(savedSupplier);
            given(supplierMapper.toResponse(savedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.createSupplier(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should().toEntity(request);
            then(supplierRepository).should().save(supplier);
            then(supplierMapper).should().toResponse(savedSupplier);
        }

        @Test
        @DisplayName("Should create supplier with empty business ID successfully")
        void shouldCreateSupplierWithEmptyBusinessIdSuccessfully() {
            // Given
            CreateSupplierRequest request = createRequestWithEmptyBusinessId();
            Supplier supplier = createSupplier();
            Supplier savedSupplier = createSupplier();
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierMapper.toEntity(request)).willReturn(supplier);
            given(supplierRepository.save(supplier)).willReturn(savedSupplier);
            given(supplierMapper.toResponse(savedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.createSupplier(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should().toEntity(request);
            then(supplierRepository).should().save(supplier);
            then(supplierMapper).should().toResponse(savedSupplier);
        }

        @Test
        @DisplayName("Should create supplier with whitespace-only business ID successfully")
        void shouldCreateSupplierWithWhitespaceOnlyBusinessIdSuccessfully() {
            // Given
            CreateSupplierRequest request = createRequestWithWhitespaceBusinessId();
            Supplier supplier = createSupplier();
            Supplier savedSupplier = createSupplier();
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierMapper.toEntity(request)).willReturn(supplier);
            given(supplierRepository.save(supplier)).willReturn(savedSupplier);
            given(supplierMapper.toResponse(savedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.createSupplier(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should().toEntity(request);
            then(supplierRepository).should().save(supplier);
            then(supplierMapper).should().toResponse(savedSupplier);
        }

        @Test
        @DisplayName("Should throw DuplicateBusinessIdException when business ID already exists")
        void shouldThrowDuplicateBusinessIdExceptionWhenBusinessIdAlreadyExists() {
            // Given
            CreateSupplierRequest request = createCompleteCreateRequest();

            given(supplierRepository.existsByBusinessIdAndActiveTrue(request.businessId())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> supplierService.createSupplier(request))
                    .isInstanceOf(DuplicateBusinessIdException.class)
                    .hasMessage("Supplier with Business ID 'BUS123456' already exists");

            then(supplierRepository).should().existsByBusinessIdAndActiveTrue(request.businessId());
            then(supplierMapper).should(never()).toEntity(any());
            then(supplierRepository).should(never()).save(any());
            then(supplierMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should check business ID existence only for non-null non-empty values")
        void shouldCheckBusinessIdExistenceOnlyForNonNullNonEmptyValues() {
            // Given
            CreateSupplierRequest requestWithValidId = createCompleteCreateRequest();
            CreateSupplierRequest requestWithNullId = createRequestWithNullBusinessId();
            CreateSupplierRequest requestWithEmptyId = createRequestWithEmptyBusinessId();
            CreateSupplierRequest requestWithWhitespaceId = createRequestWithWhitespaceBusinessId();

            Supplier supplier = createSupplier();
            Supplier savedSupplier = createSupplier();
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.existsByBusinessIdAndActiveTrue("BUS123456")).willReturn(false);
            given(supplierMapper.toEntity(any(CreateSupplierRequest.class))).willReturn(supplier);
            given(supplierRepository.save(supplier)).willReturn(savedSupplier);
            given(supplierMapper.toResponse(savedSupplier)).willReturn(expectedResponse);

            // When - Valid business ID should check existence
            supplierService.createSupplier(requestWithValidId);
            // When - Null business ID should not check existence  
            supplierService.createSupplier(requestWithNullId);
            // When - Empty business ID should not check existence
            supplierService.createSupplier(requestWithEmptyId);
            // When - Whitespace business ID should not check existence
            supplierService.createSupplier(requestWithWhitespaceId);

            // Then - Only valid business ID should trigger existence check
            then(supplierRepository).should().existsByBusinessIdAndActiveTrue("BUS123456");
        }

        @Test
        @DisplayName("Should handle mapper and repository interactions correctly")
        void shouldHandleMapperAndRepositoryInteractionsCorrectly() {
            // Given
            CreateSupplierRequest request = createCompleteCreateRequest();
            Supplier mappedSupplier = createSupplier();
            mappedSupplier.setName("Mapped Supplier");
            Supplier savedSupplier = createSupplier();
            savedSupplier.setName("Saved Supplier");
            savedSupplier.setId(UUID.randomUUID());
            SupplierResponse expectedResponse = createSupplierResponse("Final Response Supplier");

            given(supplierRepository.existsByBusinessIdAndActiveTrue(request.businessId())).willReturn(false);
            given(supplierMapper.toEntity(request)).willReturn(mappedSupplier);
            given(supplierRepository.save(mappedSupplier)).willReturn(savedSupplier);
            given(supplierMapper.toResponse(savedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.createSupplier(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.name()).isEqualTo("Final Response Supplier");
            then(supplierRepository).should().save(mappedSupplier);
            then(supplierMapper).should().toResponse(savedSupplier);
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

        private CreateSupplierRequest createRequestWithNullBusinessId() {
            return new CreateSupplierRequest(
                    "Test Supplier",
                    null, // null business ID
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-1234",
                    null, null, null, null, null, null, null
            );
        }

        private CreateSupplierRequest createRequestWithEmptyBusinessId() {
            return new CreateSupplierRequest(
                    "Test Supplier",
                    "", // empty business ID
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-1234",
                    null, null, null, null, null, null, null
            );
        }

        private CreateSupplierRequest createRequestWithWhitespaceBusinessId() {
            return new CreateSupplierRequest(
                    "Test Supplier",
                    "   ", // whitespace-only business ID
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-1234",
                    null, null, null, null, null, null, null
            );
        }
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

    @Nested
    @DisplayName("searchSuppliers() Tests")
    class SearchSuppliersTests {

        @Test
        @DisplayName("Should search suppliers with all filters")
        void shouldSearchSuppliersWithAllFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    "ABC Electronics", "contact@abc.com", "Tech City", "USA",
                    SupplierStatus.ACTIVE, SupplierType.DOMESTIC,
                    BigDecimal.valueOf(4.0), BigDecimal.valueOf(5.0), 5, 10, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
            then(supplierMapper).should().toResponse(supplier);
        }

        @Test
        @DisplayName("Should search suppliers with no filters - only active filter applied")
        void shouldSearchSuppliersWithNoFiltersOnlyActiveFilterApplied() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by name only")
        void shouldSearchSuppliersByNameOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    "ABC Electronics", null, null, null, null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by email only")
        void shouldSearchSuppliersByEmailOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, "abc@electronics.com", null, null, null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by city and country")
        void shouldSearchSuppliersByCityAndCountry() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, "Tech City", "USA", null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by status only")
        void shouldSearchSuppliersByStatusOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            supplier.setStatus(SupplierStatus.INACTIVE);
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, SupplierStatus.INACTIVE, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by supplier type only")
        void shouldSearchSuppliersBySupplierTypeOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            supplier.setSupplierType(SupplierType.INTERNATIONAL);
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, SupplierType.INTERNATIONAL, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by rating range")
        void shouldSearchSuppliersByRatingRange() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null,
                    BigDecimal.valueOf(4.0), BigDecimal.valueOf(5.0), null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by min rating only")
        void shouldSearchSuppliersByMinRatingOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null,
                    BigDecimal.valueOf(4.0), null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by max rating only")
        void shouldSearchSuppliersByMaxRatingOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null,
                    null, BigDecimal.valueOf(5.0), null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by delivery days range")
        void shouldSearchSuppliersByDeliveryDaysRange() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null, null, null, 5, 10, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by min delivery days only")
        void shouldSearchSuppliersByMinDeliveryDaysOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null, null, null, 5, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search suppliers by max delivery days only")
        void shouldSearchSuppliersByMaxDeliveryDaysOnly() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null, null, null, null, 10, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should return empty page when no suppliers match criteria")
        void shouldReturnEmptyPageWhenNoSuppliersMatchCriteria() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Supplier> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(emptyPage);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    "NonExistentSupplier", null, null, null, null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should handle multiple suppliers in search results")
        void shouldHandleMultipleSuppliersInSearchResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Supplier supplier1 = createSupplier("ABC Electronics");
            Supplier supplier2 = createSupplier("ABC Components");
            SupplierResponse response1 = createSupplierResponse("ABC Electronics");
            SupplierResponse response2 = createSupplierResponse("ABC Components");
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier1, supplier2), pageable, 2);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier1)).willReturn(response1);
            given(supplierMapper.toResponse(supplier2)).willReturn(response2);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    "ABC", null, null, null, null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(response1, response2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(supplierMapper).should().toResponse(supplier1);
            then(supplierMapper).should().toResponse(supplier2);
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(1, 5);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 15);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should ignore empty and null string filters")
        void shouldIgnoreEmptyAndNullStringFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When - test with empty strings and whitespace
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    "", "   ", null, "", null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search with mixed criteria combination")
        void shouldSearchWithMixedCriteriaCombination() {
            // Given
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    "ABC", null, "Tech City", null, SupplierStatus.ACTIVE, null,
                    BigDecimal.valueOf(4.0), null, null, 10, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(supplierResponse);
            then(supplierRepository).should().findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should preserve page metadata from repository result")
        void shouldPreservePageMetadataFromRepositoryResult() {
            // Given
            Pageable pageable = PageRequest.of(2, 10);
            Supplier supplier = createSupplier();
            SupplierResponse supplierResponse = createSupplierResponse();
            Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 35);

            given(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), eq(pageable)))
                    .willReturn(supplierPage);
            given(supplierMapper.toResponse(supplier)).willReturn(supplierResponse);

            // When
            Page<SupplierResponse> result = supplierService.searchSuppliers(
                    null, null, null, null, null, null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getTotalElements()).isEqualTo(35);
            assertThat(result.getTotalPages()).isEqualTo(4);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isFalse();
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isTrue();
        }
    }

    @Nested
    @DisplayName("getSupplierById() Tests")
    class GetSupplierByIdTests {

        @Test
        @DisplayName("Should return supplier when exists and is active")
        void shouldReturnSupplierWhenExistsAndIsActive() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier activeSupplier = createSupplier();
            activeSupplier.setId(supplierId);
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(activeSupplier));
            given(supplierMapper.toResponse(activeSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.getSupplierById(supplierId);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should().findById(supplierId);
            then(supplierMapper).should().toResponse(activeSupplier);
        }

        @Test
        @DisplayName("Should throw SupplierNotFoundException when supplier does not exist")
        void shouldThrowSupplierNotFoundExceptionWhenSupplierDoesNotExist() {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            given(supplierRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> supplierService.getSupplierById(nonExistentId))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessage("Supplier not found with id: " + nonExistentId);

            then(supplierRepository).should().findById(nonExistentId);
            then(supplierMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw SupplierNotFoundException when supplier exists but is inactive")
        void shouldThrowSupplierNotFoundExceptionWhenSupplierExistsButIsInactive() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier inactiveSupplier = createSupplier();
            inactiveSupplier.setId(supplierId);
            inactiveSupplier.softDelete(); // This sets active to false

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(inactiveSupplier));

            // When & Then
            assertThatThrownBy(() -> supplierService.getSupplierById(supplierId))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessage("Supplier not found with id: " + supplierId);

            then(supplierRepository).should().findById(supplierId);
            then(supplierMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should handle different supplier data correctly")
        void shouldHandleDifferentSupplierDataCorrectly() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier supplier = createSupplier("Tech Solutions Ltd");
            supplier.setId(supplierId);
            supplier.setEmail("contact@techsolutions.com");
            supplier.setSupplierType(SupplierType.INTERNATIONAL);
            SupplierResponse expectedResponse = createSupplierResponse("Tech Solutions Ltd");

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierMapper.toResponse(supplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.getSupplierById(supplierId);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.name()).isEqualTo("Tech Solutions Ltd");
            then(supplierRepository).should().findById(supplierId);
            then(supplierMapper).should().toResponse(supplier);
        }

        @Test
        @DisplayName("Should verify active filter is applied correctly")
        void shouldVerifyActiveFilterIsAppliedCorrectly() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierMapper.toResponse(supplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.getSupplierById(supplierId);

            // Then
            assertThat(result).isNotNull();
            then(supplierRepository).should().findById(supplierId);
            then(supplierMapper).should().toResponse(supplier);
        }

        @Test
        @DisplayName("Should verify correct exception message format")
        void shouldVerifyCorrectExceptionMessageFormat() {
            // Given
            UUID supplierId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            given(supplierRepository.findById(supplierId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> supplierService.getSupplierById(supplierId))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessage("Supplier not found with id: 123e4567-e89b-12d3-a456-426614174000");
        }
    }

    @Nested
    @DisplayName("updateSupplier() Tests")
    class UpdateSupplierTests {

        @Test
        @DisplayName("Should update supplier successfully")
        void shouldUpdateSupplierSuccessfully() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier existingSupplier = createSupplier();
            existingSupplier.setId(supplierId);
            existingSupplier.setBusinessId("OLD-BUS-ID");

            UpdateSupplierRequest request = createCompleteUpdateRequest();
            Supplier updatedSupplier = createSupplier();
            updatedSupplier.setId(supplierId);
            updatedSupplier.setName(request.name());
            updatedSupplier.setBusinessId(request.businessId());
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.existsByBusinessIdAndActiveTrue(request.businessId())).willReturn(false);
            given(supplierRepository.save(existingSupplier)).willReturn(updatedSupplier);
            given(supplierMapper.toResponse(updatedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.updateSupplier(supplierId, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should().existsByBusinessIdAndActiveTrue(request.businessId());
            then(supplierMapper).should().updateEntity(request, existingSupplier);
            then(supplierRepository).should().save(existingSupplier);
            then(supplierMapper).should().toResponse(updatedSupplier);
        }

        @Test
        @DisplayName("Should update supplier with same business ID successfully")
        void shouldUpdateSupplierWithSameBusinessIdSuccessfully() {
            // Given
            UUID supplierId = UUID.randomUUID();
            String businessId = "BUS123456";
            Supplier existingSupplier = createSupplier();
            existingSupplier.setId(supplierId);
            existingSupplier.setBusinessId(businessId);

            UpdateSupplierRequest request = createUpdateRequestWithBusinessId(businessId);
            Supplier updatedSupplier = createSupplier();
            updatedSupplier.setId(supplierId);
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.existsByBusinessIdAndActiveTrue(businessId)).willReturn(true);
            given(supplierRepository.save(existingSupplier)).willReturn(updatedSupplier);
            given(supplierMapper.toResponse(updatedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.updateSupplier(supplierId, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should().existsByBusinessIdAndActiveTrue(businessId);
            then(supplierMapper).should().updateEntity(request, existingSupplier);
            then(supplierRepository).should().save(existingSupplier);
            then(supplierMapper).should().toResponse(updatedSupplier);
        }

        @Test
        @DisplayName("Should update supplier with null business ID successfully")
        void shouldUpdateSupplierWithNullBusinessIdSuccessfully() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier existingSupplier = createSupplier();
            existingSupplier.setId(supplierId);

            UpdateSupplierRequest request = createUpdateRequestWithNullBusinessId();
            Supplier updatedSupplier = createSupplier();
            updatedSupplier.setId(supplierId);
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.save(existingSupplier)).willReturn(updatedSupplier);
            given(supplierMapper.toResponse(updatedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.updateSupplier(supplierId, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should().updateEntity(request, existingSupplier);
            then(supplierRepository).should().save(existingSupplier);
            then(supplierMapper).should().toResponse(updatedSupplier);
        }

        @Test
        @DisplayName("Should update supplier with empty business ID successfully")
        void shouldUpdateSupplierWithEmptyBusinessIdSuccessfully() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier existingSupplier = createSupplier();
            existingSupplier.setId(supplierId);

            UpdateSupplierRequest request = createUpdateRequestWithEmptyBusinessId();
            Supplier updatedSupplier = createSupplier();
            updatedSupplier.setId(supplierId);
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.save(existingSupplier)).willReturn(updatedSupplier);
            given(supplierMapper.toResponse(updatedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.updateSupplier(supplierId, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should().updateEntity(request, existingSupplier);
            then(supplierRepository).should().save(existingSupplier);
            then(supplierMapper).should().toResponse(updatedSupplier);
        }

        @Test
        @DisplayName("Should update supplier with whitespace-only business ID successfully")
        void shouldUpdateSupplierWithWhitespaceOnlyBusinessIdSuccessfully() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier existingSupplier = createSupplier();
            existingSupplier.setId(supplierId);

            UpdateSupplierRequest request = createUpdateRequestWithWhitespaceBusinessId();
            Supplier updatedSupplier = createSupplier();
            updatedSupplier.setId(supplierId);
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.save(existingSupplier)).willReturn(updatedSupplier);
            given(supplierMapper.toResponse(updatedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.updateSupplier(supplierId, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should().updateEntity(request, existingSupplier);
            then(supplierRepository).should().save(existingSupplier);
            then(supplierMapper).should().toResponse(updatedSupplier);
        }

        @Test
        @DisplayName("Should throw SupplierNotFoundException when supplier does not exist")
        void shouldThrowSupplierNotFoundExceptionWhenSupplierDoesNotExist() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            UpdateSupplierRequest request = createCompleteUpdateRequest();

            given(supplierRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> supplierService.updateSupplier(nonExistentId, request))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessage("Supplier not found with id: " + nonExistentId);

            then(supplierRepository).should().findById(nonExistentId);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should(never()).updateEntity(any(), any());
            then(supplierRepository).should(never()).save(any());
            then(supplierMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw SupplierNotFoundException when supplier exists but is inactive")
        void shouldThrowSupplierNotFoundExceptionWhenSupplierExistsButIsInactive() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier inactiveSupplier = createSupplier();
            inactiveSupplier.setId(supplierId);
            inactiveSupplier.softDelete();
            UpdateSupplierRequest request = createCompleteUpdateRequest();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(inactiveSupplier));

            // When & Then
            assertThatThrownBy(() -> supplierService.updateSupplier(supplierId, request))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessage("Supplier not found with id: " + supplierId);

            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should(never()).existsByBusinessIdAndActiveTrue(any());
            then(supplierMapper).should(never()).updateEntity(any(), any());
            then(supplierRepository).should(never()).save(any());
            then(supplierMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw DuplicateBusinessIdException when business ID already exists for different supplier")
        void shouldThrowDuplicateBusinessIdExceptionWhenBusinessIdAlreadyExistsForDifferentSupplier() {
            // Given
            UUID supplierId = UUID.randomUUID();
            String existingBusinessId = "EXISTING-BUS-ID";
            String newBusinessId = "NEW-BUS-ID";

            Supplier existingSupplier = createSupplier();
            existingSupplier.setId(supplierId);
            existingSupplier.setBusinessId(existingBusinessId);

            UpdateSupplierRequest request = createUpdateRequestWithBusinessId(newBusinessId);

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.existsByBusinessIdAndActiveTrue(newBusinessId)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> supplierService.updateSupplier(supplierId, request))
                    .isInstanceOf(DuplicateBusinessIdException.class)
                    .hasMessage("Supplier with Business ID 'NEW-BUS-ID' already exists");

            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should().existsByBusinessIdAndActiveTrue(newBusinessId);
            then(supplierMapper).should(never()).updateEntity(any(), any());
            then(supplierRepository).should(never()).save(any());
            then(supplierMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should handle mapper and repository interactions correctly")
        void shouldHandleMapperAndRepositoryInteractionsCorrectly() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier existingSupplier = createSupplier("Original Supplier");
            existingSupplier.setId(supplierId);
            existingSupplier.setBusinessId("ORIG-BUS-ID");

            UpdateSupplierRequest request = createCompleteUpdateRequest();
            Supplier updatedSupplier = createSupplier("Updated Supplier");
            updatedSupplier.setId(supplierId);
            updatedSupplier.setBusinessId(request.businessId());
            SupplierResponse expectedResponse = createSupplierResponse("Final Response Supplier");

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.existsByBusinessIdAndActiveTrue(request.businessId())).willReturn(false);
            given(supplierRepository.save(existingSupplier)).willReturn(updatedSupplier);
            given(supplierMapper.toResponse(updatedSupplier)).willReturn(expectedResponse);

            // When
            SupplierResponse result = supplierService.updateSupplier(supplierId, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.name()).isEqualTo("Final Response Supplier");
            then(supplierRepository).should().findById(supplierId);
            then(supplierMapper).should().updateEntity(request, existingSupplier);
            then(supplierRepository).should().save(existingSupplier);
            then(supplierMapper).should().toResponse(updatedSupplier);
        }

        @Test
        @DisplayName("Should verify correct exception message format for not found")
        void shouldVerifyCorrectExceptionMessageFormatForNotFound() {
            // Given
            UUID supplierId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            UpdateSupplierRequest request = createCompleteUpdateRequest();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> supplierService.updateSupplier(supplierId, request))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessage("Supplier not found with id: 123e4567-e89b-12d3-a456-426614174000");
        }

        @Test
        @DisplayName("Should check business ID existence only for non-null non-empty values")
        void shouldCheckBusinessIdExistenceOnlyForNonNullNonEmptyValues() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Supplier existingSupplier = createSupplier();
            existingSupplier.setId(supplierId);
            existingSupplier.setBusinessId("ORIG-BUS-ID");

            UpdateSupplierRequest requestWithValidId = createUpdateRequestWithBusinessId("NEW-BUS-ID");
            UpdateSupplierRequest requestWithNullId = createUpdateRequestWithNullBusinessId();
            UpdateSupplierRequest requestWithEmptyId = createUpdateRequestWithEmptyBusinessId();
            UpdateSupplierRequest requestWithWhitespaceId = createUpdateRequestWithWhitespaceBusinessId();

            Supplier updatedSupplier = createSupplier();
            updatedSupplier.setId(supplierId);
            SupplierResponse expectedResponse = createSupplierResponse();

            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(existingSupplier));
            given(supplierRepository.existsByBusinessIdAndActiveTrue("NEW-BUS-ID")).willReturn(false);
            given(supplierRepository.save(existingSupplier)).willReturn(updatedSupplier);
            given(supplierMapper.toResponse(updatedSupplier)).willReturn(expectedResponse);

            // When - Valid business ID should check existence
            supplierService.updateSupplier(supplierId, requestWithValidId);
            // When - Null business ID should not check existence
            supplierService.updateSupplier(supplierId, requestWithNullId);
            // When - Empty business ID should not check existence
            supplierService.updateSupplier(supplierId, requestWithEmptyId);
            // When - Whitespace business ID should not check existence
            supplierService.updateSupplier(supplierId, requestWithWhitespaceId);

            // Then - Only valid business ID should trigger existence check
            then(supplierRepository).should().existsByBusinessIdAndActiveTrue("NEW-BUS-ID");
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

        private UpdateSupplierRequest createUpdateRequestWithBusinessId(String businessId) {
            return new UpdateSupplierRequest(
                    "Test Supplier",
                    businessId,
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-0123",
                    null, null, null, null, null, null, null
            );
        }

        private UpdateSupplierRequest createUpdateRequestWithNullBusinessId() {
            return new UpdateSupplierRequest(
                    "Test Supplier",
                    null,
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-0123",
                    null, null, null, null, null, null, null
            );
        }

        private UpdateSupplierRequest createUpdateRequestWithEmptyBusinessId() {
            return new UpdateSupplierRequest(
                    "Test Supplier",
                    "",
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-0123",
                    null, null, null, null, null, null, null
            );
        }

        private UpdateSupplierRequest createUpdateRequestWithWhitespaceBusinessId() {
            return new UpdateSupplierRequest(
                    "Test Supplier",
                    "   ",
                    SupplierStatus.ACTIVE,
                    "test@supplier.com",
                    "+1-555-0123",
                    null, null, null, null, null, null, null
            );
        }
    }

    @Nested
    @DisplayName("getSupplierProducts() Tests")
    class GetSupplierProductsTests {

        @Test
        @DisplayName("Should return paginated products for valid supplier ID")
        void shouldReturnPaginatedProductsForValidSupplierId() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            Product activeProduct1 = createProduct("Product 1");
            Product activeProduct2 = createProduct("Product 2");
            Page<Product> productsPage = new PageImpl<>(List.of(activeProduct1, activeProduct2), pageable, 2);

            ProductResponse response1 = createProductResponse("Product 1");
            ProductResponse response2 = createProductResponse("Product 2");

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);
            given(productMapper.toResponse(activeProduct1)).willReturn(response1);
            given(productMapper.toResponse(activeProduct2)).willReturn(response2);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(response1, response2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getNumber()).isEqualTo(0);
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
            then(productMapper).should().toResponse(activeProduct1);
            then(productMapper).should().toResponse(activeProduct2);
        }

        @Test
        @DisplayName("Should only return active products")
        void shouldOnlyReturnActiveProducts() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            Product activeProduct = createProduct("Active Product");
            // Repository query handles filtering - only active products are returned
            Page<Product> productsPage = new PageImpl<>(List.of(activeProduct), pageable, 1);

            ProductResponse activeResponse = createProductResponse("Active Product");

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);
            given(productMapper.toResponse(activeProduct)).willReturn(activeResponse);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent()).containsExactly(activeResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
            then(productMapper).should().toResponse(activeProduct);
        }

        @Test
        @DisplayName("Should handle empty product list")
        void shouldHandleEmptyProductList() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            Page<Product> emptyPage = Page.empty(pageable);

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(emptyPage);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getNumber()).isEqualTo(0);
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
        }

        @Test
        @DisplayName("Should handle supplier with only inactive products")
        void shouldHandleSupplierWithOnlyInactiveProducts() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            // Repository query filters out inactive products - returns empty page
            Page<Product> emptyPage = Page.empty(pageable);

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(emptyPage);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
            then(productMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void shouldHandlePaginationCorrectly() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(1, 2); // Page 1, size 2

            Product product3 = createProduct("Product 3");
            Product product4 = createProduct("Product 4");
            // Simulate second page with 2 products out of 4 total
            Page<Product> productsPage = new PageImpl<>(List.of(product3, product4), pageable, 4);

            ProductResponse response = createProductResponse("Test Product");

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);
            given(productMapper.toResponse(any(Product.class))).willReturn(response);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
        }

        @Test
        @DisplayName("Should handle page size larger than total products")
        void shouldHandlePageSizeLargerThanTotalProducts() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 50); // Large page size

            Product product1 = createProduct("Product 1");
            Product product2 = createProduct("Product 2");
            Page<Product> productsPage = new PageImpl<>(List.of(product1, product2), pageable, 2);

            ProductResponse response1 = createProductResponse("Product 1");
            ProductResponse response2 = createProductResponse("Product 2");

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);
            given(productMapper.toResponse(product1)).willReturn(response1);
            given(productMapper.toResponse(product2)).willReturn(response2);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(response1, response2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(50);
            assertThat(result.getNumber()).isEqualTo(0);
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
        }

        @Test
        @DisplayName("Should handle page number beyond available pages")
        void shouldHandlePageNumberBeyondAvailablePages() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(2, 10); // Page 2 when only 1 product exists (only 1 page available)

            // Repository returns empty content for page beyond available data but correct total
            Page<Product> productsPage = new PageImpl<>(List.of(), pageable, 1);

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then - When requesting page beyond available data, should return empty content but correct metadata
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
        }

        @Test
        @DisplayName("Should handle first and last pages correctly")
        void shouldHandleFirstAndLastPagesCorrectly() {
            // Given
            UUID supplierId = UUID.randomUUID();

            List<Product> firstPageProducts = List.of(
                    createProduct("Product 1"),
                    createProduct("Product 2")
            );
            List<Product> lastPageProducts = List.of(
                    createProduct("Product 3")
            );

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(productMapper.toResponse(any())).willReturn(createProductResponse("Test Product"));

            // When - First page
            Pageable firstPage = PageRequest.of(0, 2);
            Page<Product> firstPageResult = new PageImpl<>(firstPageProducts, firstPage, 3);
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, firstPage)).willReturn(firstPageResult);
            Page<ProductResponse> firstResult = supplierService.getSupplierProducts(supplierId, firstPage);

            // Then - First page
            assertThat(firstResult.isFirst()).isTrue();
            assertThat(firstResult.isLast()).isFalse();
            assertThat(firstResult.hasNext()).isTrue();
            assertThat(firstResult.hasPrevious()).isFalse();

            // When - Last page
            Pageable lastPage = PageRequest.of(1, 2);
            Page<Product> lastPageResult = new PageImpl<>(lastPageProducts, lastPage, 3);
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, lastPage)).willReturn(lastPageResult);
            Page<ProductResponse> lastResult = supplierService.getSupplierProducts(supplierId, lastPage);

            // Then - Last page
            assertThat(lastResult.isFirst()).isFalse();
            assertThat(lastResult.isLast()).isTrue();
            assertThat(lastResult.hasNext()).isFalse();
            assertThat(lastResult.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should throw SupplierNotFoundException for invalid supplier ID")
        void shouldThrowSupplierNotFoundExceptionForInvalidSupplierId() {
            // Given
            UUID invalidSupplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            given(supplierRepository.findById(invalidSupplierId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> supplierService.getSupplierProducts(invalidSupplierId, pageable))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessageContaining(invalidSupplierId.toString());

            then(supplierRepository).should().findById(invalidSupplierId);
            then(supplierRepository).should(never()).findActiveProductsBySupplierId(any(), any());
            then(productMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw SupplierNotFoundException for inactive supplier")
        void shouldThrowSupplierNotFoundExceptionForInactiveSupplier() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            Supplier inactiveSupplier = createSupplier();
            inactiveSupplier.setId(supplierId);
            inactiveSupplier.softDelete(); // This sets active to false
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(inactiveSupplier));

            // When & Then
            assertThatThrownBy(() -> supplierService.getSupplierProducts(supplierId, pageable))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessageContaining(supplierId.toString());

            then(supplierRepository).should().findById(supplierId);
            then(supplierRepository).should(never()).findActiveProductsBySupplierId(any(), any());
            then(productMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should handle large datasets with correct pagination")
        void shouldHandleLargeDatasetsWithCorrectPagination() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(2, 3); // Page 2, size 3

            // Create 2 products for the last page (page 2 of 8 total products)
            List<Product> pageProducts = List.of(
                    createProduct("Product 7"),
                    createProduct("Product 8")
            );
            Page<Product> productsPage = new PageImpl<>(pageProducts, pageable, 8);

            ProductResponse response = createProductResponse("Test Product");

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);
            given(productMapper.toResponse(any(Product.class))).willReturn(response);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2); // Last 2 products on this page
            assertThat(result.getTotalElements()).isEqualTo(8);
            assertThat(result.getTotalPages()).isEqualTo(3); // 8 items / 3 per page = 3 pages
            assertThat(result.getSize()).isEqualTo(3);
            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
        }

        @Test
        @DisplayName("Should handle mixed active and inactive products correctly")
        void shouldHandleMixedActiveAndInactiveProductsCorrectly() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            Product activeProduct1 = createProduct("Active Product 1");
            Product activeProduct2 = createProduct("Active Product 2");
            // Repository query filters out inactive products - only active products returned
            Page<Product> productsPage = new PageImpl<>(List.of(activeProduct1, activeProduct2), pageable, 2);

            ProductResponse response1 = createProductResponse("Active Product 1");
            ProductResponse response2 = createProductResponse("Active Product 2");

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);
            given(productMapper.toResponse(activeProduct1)).willReturn(response1);
            given(productMapper.toResponse(activeProduct2)).willReturn(response2);

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(response1, response2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(supplierRepository).should().findActiveProductsBySupplierId(supplierId, pageable);
            then(productMapper).should().toResponse(activeProduct1);
            then(productMapper).should().toResponse(activeProduct2);
        }

        @Test
        @DisplayName("Should verify PageImpl is created with correct parameters")
        void shouldVerifyPageImplIsCreatedWithCorrectParameters() {
            // Given
            UUID supplierId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(1, 3);

            List<Product> pageProducts = List.of(
                    createProduct("Product 4"),
                    createProduct("Product 5")
            );
            Page<Product> productsPage = new PageImpl<>(pageProducts, pageable, 5);

            Supplier supplier = createSupplier();
            supplier.setId(supplierId);
            given(supplierRepository.findById(supplierId)).willReturn(Optional.of(supplier));
            given(supplierRepository.findActiveProductsBySupplierId(supplierId, pageable)).willReturn(productsPage);
            given(productMapper.toResponse(any())).willReturn(createProductResponse("Test Product"));

            // When
            Page<ProductResponse> result = supplierService.getSupplierProducts(supplierId, pageable);

            // Then - Verify PageImpl metadata
            assertThat(result.getContent()).hasSize(2); // Products 4 and 5
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(3);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getNumberOfElements()).isEqualTo(2);
            assertThat(result.getSort()).isEqualTo(pageable.getSort());
        }

        private Product createProduct(String name) {
            Product product = new Product();
            product.setId(UUID.randomUUID());
            product.setName(name);
            product.setSku("SKU-" + name.replaceAll(" ", "").toUpperCase());
            product.setPrice(BigDecimal.valueOf(100.0));
            product.setStockQuantity(10);
            product.setMinStockLevel(5);
            product.setCategory("electronics");
            return product;
        }

        private ProductResponse createProductResponse(String name) {
            return new ProductResponse(
                    UUID.randomUUID(),
                    name,
                    "Description for " + name,
                    "SKU-" + name.replaceAll(" ", "").toUpperCase(),
                    BigDecimal.valueOf(100.0),
                    10,
                    5,
                    "electronics",
                    true,
                    false,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    List.of()
            );
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