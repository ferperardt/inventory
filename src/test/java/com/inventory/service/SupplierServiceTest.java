package com.inventory.service;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Address;
import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.exception.DuplicateBusinessIdException;
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

    private SupplierService supplierService;

    @BeforeEach
    void setUp() {
        supplierService = new SupplierService(supplierRepository, supplierMapper);
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