package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.request.UpdateSupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.exception.DuplicateBusinessIdException;
import com.inventory.exception.SupplierNotFoundException;
import com.inventory.exception.GlobalExceptionHandler;
import com.inventory.service.SupplierService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierController Tests")
class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new SupplierController(supplierService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/suppliers")
    class CreateSupplierTests {

        @Test
        @DisplayName("Should create supplier successfully")
        void shouldCreateSupplierSuccessfully() throws Exception {
            // Given
            CreateSupplierRequest request = createCompleteSupplierRequest();
            SupplierResponse response = createCompleteSupplierResponse();

            given(supplierService.createSupplier(any(CreateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.businessId").value("BUS123456"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.email").value("contact@abcelectronics.com"))
                    .andExpect(jsonPath("$.phone").value("+1-555-0123"))
                    .andExpect(jsonPath("$.contactPerson").value("John Smith"))
                    .andExpect(jsonPath("$.address.streetAddress").value("123 Industrial Blvd"))
                    .andExpect(jsonPath("$.address.city").value("Tech City"))
                    .andExpect(jsonPath("$.address.stateProvince").value("CA"))
                    .andExpect(jsonPath("$.address.postalCode").value("90210"))
                    .andExpect(jsonPath("$.address.country").value("USA"))
                    .andExpect(jsonPath("$.paymentTerms").value("NET30"))
                    .andExpect(jsonPath("$.averageDeliveryDays").value(7))
                    .andExpect(jsonPath("$.supplierType").value("DOMESTIC"))
                    .andExpect(jsonPath("$.notes").value("Reliable supplier with good quality products"))
                    .andExpect(jsonPath("$.rating").value(4.5))
                    .andExpect(jsonPath("$.active").value(true));

            then(supplierService).should().createSupplier(any(CreateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should create supplier with minimal required fields")
        void shouldCreateSupplierWithMinimalRequiredFields() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Basic Supplier", null, null, "basic@supplier.com", "+1-555-1234",
                    null, null, null, null, null, null, null
            );
            SupplierResponse response = new SupplierResponse(
                    UUID.randomUUID(), "Basic Supplier", null, SupplierStatus.ACTIVE,
                    "basic@supplier.com", "+1-555-1234", null, null,
                    null, null, null, null, null,
                    true, LocalDateTime.now(), LocalDateTime.now()
            );

            given(supplierService.createSupplier(any(CreateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("Basic Supplier"))
                    .andExpect(jsonPath("$.businessId").doesNotExist())
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.email").value("basic@supplier.com"))
                    .andExpect(jsonPath("$.phone").value("+1-555-1234"))
                    .andExpect(jsonPath("$.active").value(true));

            then(supplierService).should().createSupplier(any(CreateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "", "BUS123456", SupplierStatus.ACTIVE, "contact@test.com", "+1-555-0123",
                    null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.name").value("Supplier name is required"));
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "invalid-email",
                    "+1-555-0123", null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"));
        }

        @Test
        @DisplayName("Should return 400 when email is blank")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "",
                    "+1-555-0123", null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.email").value("Email is required"));
        }

        @Test
        @DisplayName("Should return 400 when phone is blank")
        void shouldReturn400WhenPhoneIsBlank() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "valid@email.com",
                    "", null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.phone").value("Phone is required"));
        }

        @Test
        @DisplayName("Should return 400 when fields exceed size limits")
        void shouldReturn400WhenFieldsExceedSizeLimits() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "A".repeat(151), // Exceeds 150 character limit
                    "B".repeat(51),  // Exceeds 50 character limit
                    SupplierStatus.ACTIVE,
                    "valid@email.com",
                    "+1-555-0123",
                    null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.name").value("Supplier name must not exceed 150 characters"))
                    .andExpect(jsonPath("$.fieldErrors.businessId").value("Business ID must not exceed 50 characters"));
        }

        @Test
        @DisplayName("Should return 400 when rating is out of range")
        void shouldReturn400WhenRatingIsOutOfRange() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "valid@email.com",
                    "+1-555-0123", null, null, null, null, null,
                    null, BigDecimal.valueOf(6.0) // Exceeds max rating of 5.0
            );

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.rating").value("Rating must be between 1.0 and 5.0"));
        }

        @Test
        @DisplayName("Should return 400 when averageDeliveryDays is less than 1")
        void shouldReturn400WhenAverageDeliveryDaysIsLessThan1() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "valid@email.com",
                    "+1-555-0123", null, null, null, 0, // Less than minimum 1
                    null, null, null
            );

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.averageDeliveryDays").value("Average delivery days must be at least 1"));
        }

        @Test
        @DisplayName("Should return 409 when business ID already exists")
        void shouldReturn409WhenBusinessIdAlreadyExists() throws Exception {
            // Given
            CreateSupplierRequest request = createCompleteSupplierRequest();

            given(supplierService.createSupplier(any(CreateSupplierRequest.class)))
                    .willThrow(new DuplicateBusinessIdException("BUS123456"));

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Duplicate Business ID"))
                    .andExpect(jsonPath("$.message").value("Supplier with Business ID 'BUS123456' already exists"));

            then(supplierService).should().createSupplier(any(CreateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should set default status to ACTIVE when status is null")
        void shouldSetDefaultStatusToActiveWhenStatusIsNull() throws Exception {
            // Given
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", null, // null status should default to ACTIVE
                    "test@supplier.com", "+1-555-0123", null, null, null, null,
                    null, null, null
            );
            SupplierResponse response = new SupplierResponse(
                    UUID.randomUUID(), "Test Supplier", "BUS123456", SupplierStatus.ACTIVE,
                    "test@supplier.com", "+1-555-0123", null, null,
                    null, null, null, null, null,
                    true, LocalDateTime.now(), LocalDateTime.now()
            );

            given(supplierService.createSupplier(any(CreateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            then(supplierService).should().createSupplier(any(CreateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should handle valid address in request")
        void shouldHandleValidAddressInRequest() throws Exception {
            // Given
            Address address = new Address("123 Test St", "Test City", "TS", "12345", "USA");
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, address, null, null, null, null, null
            );
            SupplierResponse response = new SupplierResponse(
                    UUID.randomUUID(), "Test Supplier", "BUS123456", SupplierStatus.ACTIVE,
                    "test@supplier.com", "+1-555-0123", null, address,
                    null, null, null, null, null,
                    true, LocalDateTime.now(), LocalDateTime.now()
            );

            given(supplierService.createSupplier(any(CreateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/suppliers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.address.streetAddress").value("123 Test St"))
                    .andExpect(jsonPath("$.address.city").value("Test City"))
                    .andExpect(jsonPath("$.address.stateProvince").value("TS"))
                    .andExpect(jsonPath("$.address.postalCode").value("12345"))
                    .andExpect(jsonPath("$.address.country").value("USA"));

            then(supplierService).should().createSupplier(any(CreateSupplierRequest.class));
        }

        private CreateSupplierRequest createCompleteSupplierRequest() {
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
    @DisplayName("GET /api/v1/suppliers")
    class GetAllSuppliersTests {

        @Test
        @DisplayName("Should return paginated suppliers")
        void shouldReturnPaginatedSuppliers() throws Exception {
            // Given
            SupplierResponse supplier = createSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.getAllSuppliers(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.content[0].email").value("contact@abcelectronics.com"))
                    .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(20));

            then(supplierService).should().getAllSuppliers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no suppliers")
        void shouldReturnEmptyPageWhenNoSuppliers() throws Exception {
            // Given
            Page<SupplierResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            given(supplierService.getAllSuppliers(any(Pageable.class))).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.size").value(20));

            then(supplierService).should().getAllSuppliers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle pagination parameters correctly")
        void shouldHandlePaginationParametersCorrectly() throws Exception {
            // Given
            SupplierResponse supplier = createSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(1, 10), 25);

            given(supplierService.getAllSuppliers(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort", "name,asc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.totalElements").value(25))
                    .andExpect(jsonPath("$.totalPages").value(3));

            then(supplierService).should().getAllSuppliers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should use default page size of 20 when not specified")
        void shouldUseDefaultPageSizeWhenNotSpecified() throws Exception {
            // Given
            SupplierResponse supplier = createSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.getAllSuppliers(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(20));

            then(supplierService).should().getAllSuppliers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should sort by name by default")
        void shouldSortByNameByDefault() throws Exception {
            // Given
            SupplierResponse supplier1 = createSupplierResponse("ABC Electronics Ltd");
            SupplierResponse supplier2 = createSupplierResponse("XYZ Components Inc");
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier1, supplier2), PageRequest.of(0, 20), 2);

            given(supplierService.getAllSuppliers(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.content[1].name").value("XYZ Components Inc"));

            then(supplierService).should().getAllSuppliers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return suppliers with all expected fields")
        void shouldReturnSuppliersWithAllExpectedFields() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.getAllSuppliers(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").exists())
                    .andExpect(jsonPath("$.content[0].name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.content[0].businessId").value("BUS123456"))
                    .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$.content[0].email").value("contact@abcelectronics.com"))
                    .andExpect(jsonPath("$.content[0].phone").value("+1-555-0123"))
                    .andExpect(jsonPath("$.content[0].contactPerson").value("John Smith"))
                    .andExpect(jsonPath("$.content[0].address.streetAddress").value("123 Industrial Blvd"))
                    .andExpect(jsonPath("$.content[0].address.city").value("Tech City"))
                    .andExpect(jsonPath("$.content[0].address.stateProvince").value("CA"))
                    .andExpect(jsonPath("$.content[0].address.postalCode").value("90210"))
                    .andExpect(jsonPath("$.content[0].address.country").value("USA"))
                    .andExpect(jsonPath("$.content[0].paymentTerms").value("NET30"))
                    .andExpect(jsonPath("$.content[0].averageDeliveryDays").value(7))
                    .andExpect(jsonPath("$.content[0].supplierType").value("DOMESTIC"))
                    .andExpect(jsonPath("$.content[0].notes").value("Reliable supplier with good quality products"))
                    .andExpect(jsonPath("$.content[0].rating").value(4.5))
                    .andExpect(jsonPath("$.content[0].active").value(true))
                    .andExpect(jsonPath("$.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.content[0].updatedAt").exists());

            then(supplierService).should().getAllSuppliers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/suppliers/{id}")
    class GetSupplierByIdTests {

        @Test
        @DisplayName("Should return supplier when found")
        void shouldReturnSupplierWhenFound() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            SupplierResponse supplierResponse = createCompleteSupplierResponse();

            given(supplierService.getSupplierById(supplierId)).willReturn(supplierResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/{id}", supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.businessId").value("BUS123456"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.email").value("contact@abcelectronics.com"))
                    .andExpect(jsonPath("$.phone").value("+1-555-0123"))
                    .andExpect(jsonPath("$.contactPerson").value("John Smith"))
                    .andExpect(jsonPath("$.address.streetAddress").value("123 Industrial Blvd"))
                    .andExpect(jsonPath("$.address.city").value("Tech City"))
                    .andExpect(jsonPath("$.address.stateProvince").value("CA"))
                    .andExpect(jsonPath("$.address.postalCode").value("90210"))
                    .andExpect(jsonPath("$.address.country").value("USA"))
                    .andExpect(jsonPath("$.paymentTerms").value("NET30"))
                    .andExpect(jsonPath("$.averageDeliveryDays").value(7))
                    .andExpect(jsonPath("$.supplierType").value("DOMESTIC"))
                    .andExpect(jsonPath("$.notes").value("Reliable supplier with good quality products"))
                    .andExpect(jsonPath("$.rating").value(4.5))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            then(supplierService).should().getSupplierById(supplierId);
        }

        @Test
        @DisplayName("Should return 404 when supplier not found")
        void shouldReturn404WhenSupplierNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            given(supplierService.getSupplierById(nonExistentId))
                    .willThrow(new SupplierNotFoundException(nonExistentId));

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Supplier Not Found"))
                    .andExpect(jsonPath("$.message").value("Supplier not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            then(supplierService).should().getSupplierById(nonExistentId);
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void shouldReturn400ForInvalidUuidFormat() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/{id}", "invalid-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                    .andExpect(jsonPath("$.parameter").value("id"))
                    .andExpect(jsonPath("$.invalidValue").value("invalid-uuid"))
                    .andExpect(jsonPath("$.expectedType").value("UUID"));
        }

        @Test
        @DisplayName("Should handle supplier with minimal data")
        void shouldHandleSupplierWithMinimalData() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            SupplierResponse minimalSupplier = new SupplierResponse(
                    supplierId, "Minimal Supplier", null, SupplierStatus.ACTIVE,
                    "minimal@supplier.com", "+1-555-9999", null, null,
                    null, null, null, null, null,
                    true, LocalDateTime.now(), LocalDateTime.now()
            );

            given(supplierService.getSupplierById(supplierId)).willReturn(minimalSupplier);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/{id}", supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("Minimal Supplier"))
                    .andExpect(jsonPath("$.email").value("minimal@supplier.com"))
                    .andExpect(jsonPath("$.phone").value("+1-555-9999"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.businessId").doesNotExist())
                    .andExpect(jsonPath("$.contactPerson").doesNotExist())
                    .andExpect(jsonPath("$.address").doesNotExist());

            then(supplierService).should().getSupplierById(supplierId);
        }

        @Test
        @DisplayName("Should handle supplier with different status")
        void shouldHandleSupplierWithDifferentStatus() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            SupplierResponse supplierResponse = createSupplierResponse("Test Supplier");
            SupplierResponse inactiveSupplier = new SupplierResponse(
                    supplierResponse.id(), supplierResponse.name(), supplierResponse.businessId(),
                    SupplierStatus.INACTIVE, supplierResponse.email(), supplierResponse.phone(),
                    supplierResponse.contactPerson(), supplierResponse.address(), supplierResponse.paymentTerms(),
                    supplierResponse.averageDeliveryDays(), supplierResponse.supplierType(),
                    supplierResponse.notes(), supplierResponse.rating(), supplierResponse.active(),
                    supplierResponse.createdAt(), supplierResponse.updatedAt()
            );

            given(supplierService.getSupplierById(supplierId)).willReturn(inactiveSupplier);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/{id}", supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("INACTIVE"));

            then(supplierService).should().getSupplierById(supplierId);
        }

        @Test
        @DisplayName("Should handle supplier with different supplier type")
        void shouldHandleSupplierWithDifferentSupplierType() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            SupplierResponse supplierResponse = createSupplierResponse("International Supplier");
            SupplierResponse internationalSupplier = new SupplierResponse(
                    supplierResponse.id(), supplierResponse.name(), supplierResponse.businessId(),
                    supplierResponse.status(), supplierResponse.email(), supplierResponse.phone(),
                    supplierResponse.contactPerson(), supplierResponse.address(), supplierResponse.paymentTerms(),
                    supplierResponse.averageDeliveryDays(), SupplierType.INTERNATIONAL,
                    supplierResponse.notes(), supplierResponse.rating(), supplierResponse.active(),
                    supplierResponse.createdAt(), supplierResponse.updatedAt()
            );

            given(supplierService.getSupplierById(supplierId)).willReturn(internationalSupplier);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/{id}", supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.supplierType").value("INTERNATIONAL"));

            then(supplierService).should().getSupplierById(supplierId);
        }

        @Test
        @DisplayName("Should verify all response fields are present")
        void shouldVerifyAllResponseFieldsArePresent() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            SupplierResponse completeResponse = createCompleteSupplierResponse();

            given(supplierService.getSupplierById(supplierId)).willReturn(completeResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/{id}", supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.businessId").exists())
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.email").exists())
                    .andExpect(jsonPath("$.phone").exists())
                    .andExpect(jsonPath("$.contactPerson").exists())
                    .andExpect(jsonPath("$.address").exists())
                    .andExpect(jsonPath("$.paymentTerms").exists())
                    .andExpect(jsonPath("$.averageDeliveryDays").exists())
                    .andExpect(jsonPath("$.supplierType").exists())
                    .andExpect(jsonPath("$.notes").exists())
                    .andExpect(jsonPath("$.rating").exists())
                    .andExpect(jsonPath("$.active").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            then(supplierService).should().getSupplierById(supplierId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/suppliers/search")
    class SearchSuppliersTests {

        @Test
        @DisplayName("Should search suppliers with all filters")
        void shouldSearchSuppliersWithAllFilters() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq("ABC Electronics"), eq("contact@abcelectronics.com"), eq("Tech City"), 
                    eq("USA"), eq(SupplierStatus.ACTIVE), eq(SupplierType.DOMESTIC), 
                    eq(BigDecimal.valueOf(4.0)), eq(BigDecimal.valueOf(5.0)), 
                    eq(5), eq(10), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("name", "ABC Electronics")
                            .param("email", "contact@abcelectronics.com")
                            .param("city", "Tech City")
                            .param("country", "USA")
                            .param("status", "ACTIVE")
                            .param("supplierType", "DOMESTIC")
                            .param("minRating", "4.0")
                            .param("maxRating", "5.0")
                            .param("minDeliveryDays", "5")
                            .param("maxDeliveryDays", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$.content[0].supplierType").value("DOMESTIC"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should search suppliers without any filters")
        void shouldSearchSuppliersWithoutAnyFilters() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].name").value("ABC Electronics Ltd"));
        }

        @Test
        @DisplayName("Should search suppliers by name only")
        void shouldSearchSuppliersByNameOnly() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq("ABC"), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("name", "ABC"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].name").value("ABC Electronics Ltd"));
        }

        @Test
        @DisplayName("Should search suppliers by email only")
        void shouldSearchSuppliersByEmailOnly() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq("abcelectronics"), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("email", "abcelectronics"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].email").value("contact@abcelectronics.com"));
        }

        @Test
        @DisplayName("Should search suppliers by city and country")
        void shouldSearchSuppliersByCityAndCountry() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq("Tech City"), eq("USA"), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("city", "Tech City")
                            .param("country", "USA"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].address.city").value("Tech City"))
                    .andExpect(jsonPath("$.content[0].address.country").value("USA"));
        }

        @Test
        @DisplayName("Should search suppliers by status only")
        void shouldSearchSuppliersByStatusOnly() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            supplier = new SupplierResponse(supplier.id(), supplier.name(), supplier.businessId(),
                    SupplierStatus.INACTIVE, supplier.email(), supplier.phone(), supplier.contactPerson(),
                    supplier.address(), supplier.paymentTerms(), supplier.averageDeliveryDays(),
                    supplier.supplierType(), supplier.notes(), supplier.rating(), supplier.active(),
                    supplier.createdAt(), supplier.updatedAt());
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq(null), eq(null), eq(SupplierStatus.INACTIVE), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("status", "INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].status").value("INACTIVE"));
        }

        @Test
        @DisplayName("Should search suppliers by supplier type only")
        void shouldSearchSuppliersBySupplierTypeOnly() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            supplier = new SupplierResponse(supplier.id(), supplier.name(), supplier.businessId(),
                    supplier.status(), supplier.email(), supplier.phone(), supplier.contactPerson(),
                    supplier.address(), supplier.paymentTerms(), supplier.averageDeliveryDays(),
                    SupplierType.INTERNATIONAL, supplier.notes(), supplier.rating(), supplier.active(),
                    supplier.createdAt(), supplier.updatedAt());
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq(null), eq(null), eq(null), eq(SupplierType.INTERNATIONAL),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("supplierType", "INTERNATIONAL"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].supplierType").value("INTERNATIONAL"));
        }

        @Test
        @DisplayName("Should search suppliers by rating range")
        void shouldSearchSuppliersByRatingRange() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(BigDecimal.valueOf(4.0)), eq(BigDecimal.valueOf(5.0)), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("minRating", "4.0")
                            .param("maxRating", "5.0"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].rating").value(4.5));
        }

        @Test
        @DisplayName("Should search suppliers by delivery days range")
        void shouldSearchSuppliersByDeliveryDaysRange() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(5), eq(10), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("minDeliveryDays", "5")
                            .param("maxDeliveryDays", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].averageDeliveryDays").value(7));
        }

        @Test
        @DisplayName("Should return empty page when no suppliers match criteria")
        void shouldReturnEmptyPageWhenNoSuppliersMatchCriteria() throws Exception {
            // Given
            Page<SupplierResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            given(supplierService.searchSuppliers(
                    eq("NonExistentSupplier"), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("name", "NonExistentSupplier"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should handle pagination parameters correctly")
        void shouldHandlePaginationParametersCorrectly() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(1, 5), 10);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("page", "1")
                            .param("size", "5")
                            .param("sort", "name,desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.totalElements").value(10))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        @DisplayName("Should handle long parameter values gracefully")
        void shouldHandleLongParameterValuesGracefully() throws Exception {
            // Given
            String longName = "A".repeat(151); // Exceeds 150 character limit
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(longName), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then - Should handle gracefully, service will filter based on business logic
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("name", longName))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should return 400 when invalid status provided")
        void shouldReturn400WhenInvalidStatusProvided() throws Exception {
            // When & Then - Invalid enum values should be handled by Spring and return 400
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("status", "INVALID_STATUS"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when invalid supplier type provided")
        void shouldReturn400WhenInvalidSupplierTypeProvided() throws Exception {
            // When & Then - Invalid enum values should be handled by Spring and return 400
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("supplierType", "INVALID_TYPE"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should search suppliers with mixed criteria")
        void shouldSearchSuppliersWithMixedCriteria() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq("ABC"), eq(null), eq("Tech"), eq(null), eq(SupplierStatus.ACTIVE), eq(null),
                    eq(BigDecimal.valueOf(4.0)), eq(null), eq(null), eq(10), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search")
                            .param("name", "ABC")
                            .param("city", "Tech")
                            .param("status", "ACTIVE")
                            .param("minRating", "4.0")
                            .param("maxDeliveryDays", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should use default pagination when not specified")
        void shouldUseDefaultPaginationWhenNotSpecified() throws Exception {
            // Given
            SupplierResponse supplier = createCompleteSupplierResponse();
            Page<SupplierResponse> page = new PageImpl<>(List.of(supplier), PageRequest.of(0, 20), 1);

            given(supplierService.searchSuppliers(
                    eq(null), eq(null), eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/suppliers/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/suppliers/{id}")
    class UpdateSupplierTests {

        @Test
        @DisplayName("Should update supplier successfully")
        void shouldUpdateSupplierSuccessfully() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = createCompleteUpdateSupplierRequest();
            SupplierResponse response = createCompleteSupplierResponse();

            given(supplierService.updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("ABC Electronics Ltd"))
                    .andExpect(jsonPath("$.businessId").value("BUS123456"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.email").value("contact@abcelectronics.com"))
                    .andExpect(jsonPath("$.phone").value("+1-555-0123"))
                    .andExpect(jsonPath("$.contactPerson").value("John Smith"))
                    .andExpect(jsonPath("$.address.streetAddress").value("123 Industrial Blvd"))
                    .andExpect(jsonPath("$.address.city").value("Tech City"))
                    .andExpect(jsonPath("$.address.stateProvince").value("CA"))
                    .andExpect(jsonPath("$.address.postalCode").value("90210"))
                    .andExpect(jsonPath("$.address.country").value("USA"))
                    .andExpect(jsonPath("$.paymentTerms").value("NET30"))
                    .andExpect(jsonPath("$.averageDeliveryDays").value(7))
                    .andExpect(jsonPath("$.supplierType").value("DOMESTIC"))
                    .andExpect(jsonPath("$.notes").value("Reliable supplier with good quality products"))
                    .andExpect(jsonPath("$.rating").value(4.5))
                    .andExpect(jsonPath("$.active").value(true));

            then(supplierService).should().updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should update supplier with minimal required fields")
        void shouldUpdateSupplierWithMinimalRequiredFields() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Basic Supplier", null, null, "basic@supplier.com", "+1-555-1234",
                    null, null, null, null, null, null, null
            );
            SupplierResponse response = new SupplierResponse(
                    supplierId, "Basic Supplier", null, SupplierStatus.ACTIVE,
                    "basic@supplier.com", "+1-555-1234", null, null,
                    null, null, null, null, null,
                    true, LocalDateTime.now(), LocalDateTime.now()
            );

            given(supplierService.updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("Basic Supplier"))
                    .andExpect(jsonPath("$.businessId").doesNotExist())
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.email").value("basic@supplier.com"))
                    .andExpect(jsonPath("$.phone").value("+1-555-1234"))
                    .andExpect(jsonPath("$.active").value(true));

            then(supplierService).should().updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "", "BUS123456", SupplierStatus.ACTIVE, "contact@test.com", "+1-555-0123",
                    null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.name").value("Supplier name is required"));
        }

        @Test
        @DisplayName("Should return 400 when email is invalid")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "invalid-email",
                    "+1-555-0123", null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"));
        }

        @Test
        @DisplayName("Should return 400 when email is blank")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "",
                    "+1-555-0123", null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.email").value("Email is required"));
        }

        @Test
        @DisplayName("Should return 400 when phone is blank")
        void shouldReturn400WhenPhoneIsBlank() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "valid@email.com",
                    "", null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.phone").value("Phone is required"));
        }

        @Test
        @DisplayName("Should return 400 when fields exceed size limits")
        void shouldReturn400WhenFieldsExceedSizeLimits() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "A".repeat(151), // Exceeds 150 character limit
                    "B".repeat(51),  // Exceeds 50 character limit
                    SupplierStatus.ACTIVE,
                    "valid@email.com",
                    "+1-555-0123",
                    null, null, null, null, null, null, null
            );

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.name").value("Supplier name must not exceed 150 characters"))
                    .andExpect(jsonPath("$.fieldErrors.businessId").value("Business ID must not exceed 50 characters"));
        }

        @Test
        @DisplayName("Should return 400 when rating is out of range")
        void shouldReturn400WhenRatingIsOutOfRange() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "valid@email.com",
                    "+1-555-0123", null, null, null, null, null,
                    null, BigDecimal.valueOf(6.0) // Exceeds max rating of 5.0
            );

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.rating").value("Rating must be between 1.0 and 5.0"));
        }

        @Test
        @DisplayName("Should return 400 when averageDeliveryDays is less than 1")
        void shouldReturn400WhenAverageDeliveryDaysIsLessThan1() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Valid Supplier", "BUS123456", SupplierStatus.ACTIVE, "valid@email.com",
                    "+1-555-0123", null, null, null, 0, // Less than minimum 1
                    null, null, null
            );

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.averageDeliveryDays").value("Average delivery days must be at least 1"));
        }

        @Test
        @DisplayName("Should return 404 when supplier not found")
        void shouldReturn404WhenSupplierNotFound() throws Exception {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            UpdateSupplierRequest request = createCompleteUpdateSupplierRequest();

            given(supplierService.updateSupplier(eq(nonExistentId), any(UpdateSupplierRequest.class)))
                    .willThrow(new SupplierNotFoundException(nonExistentId));

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Supplier Not Found"))
                    .andExpect(jsonPath("$.message").value("Supplier not found with id: " + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").exists());

            then(supplierService).should().updateSupplier(eq(nonExistentId), any(UpdateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should return 409 when business ID already exists")
        void shouldReturn409WhenBusinessIdAlreadyExists() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = createCompleteUpdateSupplierRequest();

            given(supplierService.updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class)))
                    .willThrow(new DuplicateBusinessIdException("BUS123456"));

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Duplicate Business ID"))
                    .andExpect(jsonPath("$.message").value("Supplier with Business ID 'BUS123456' already exists"));

            then(supplierService).should().updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void shouldReturn400ForInvalidUuidFormat() throws Exception {
            // Given
            UpdateSupplierRequest request = createCompleteUpdateSupplierRequest();

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", "invalid-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                    .andExpect(jsonPath("$.parameter").value("id"))
                    .andExpect(jsonPath("$.invalidValue").value("invalid-uuid"))
                    .andExpect(jsonPath("$.expectedType").value("UUID"));
        }

        @Test
        @DisplayName("Should handle valid address in request")
        void shouldHandleValidAddressInRequest() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            Address address = new Address("123 Test St", "Test City", "TS", "12345", "USA");
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "Test Supplier", "BUS123456", SupplierStatus.ACTIVE, "test@supplier.com",
                    "+1-555-0123", null, address, null, null, null, null, null
            );
            SupplierResponse response = new SupplierResponse(
                    supplierId, "Test Supplier", "BUS123456", SupplierStatus.ACTIVE,
                    "test@supplier.com", "+1-555-0123", null, address,
                    null, null, null, null, null,
                    true, LocalDateTime.now(), LocalDateTime.now()
            );

            given(supplierService.updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.address.streetAddress").value("123 Test St"))
                    .andExpect(jsonPath("$.address.city").value("Test City"))
                    .andExpect(jsonPath("$.address.stateProvince").value("TS"))
                    .andExpect(jsonPath("$.address.postalCode").value("12345"))
                    .andExpect(jsonPath("$.address.country").value("USA"));

            then(supplierService).should().updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class));
        }

        @Test
        @DisplayName("Should verify all response fields are present after update")
        void shouldVerifyAllResponseFieldsArePresentAfterUpdate() throws Exception {
            // Given
            UUID supplierId = UUID.randomUUID();
            UpdateSupplierRequest request = createCompleteUpdateSupplierRequest();
            SupplierResponse response = createCompleteSupplierResponse();

            given(supplierService.updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/suppliers/{id}", supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.businessId").exists())
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.email").exists())
                    .andExpect(jsonPath("$.phone").exists())
                    .andExpect(jsonPath("$.contactPerson").exists())
                    .andExpect(jsonPath("$.address").exists())
                    .andExpect(jsonPath("$.paymentTerms").exists())
                    .andExpect(jsonPath("$.averageDeliveryDays").exists())
                    .andExpect(jsonPath("$.supplierType").exists())
                    .andExpect(jsonPath("$.notes").exists())
                    .andExpect(jsonPath("$.rating").exists())
                    .andExpect(jsonPath("$.active").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            then(supplierService).should().updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class));
        }

        private UpdateSupplierRequest createCompleteUpdateSupplierRequest() {
            return new UpdateSupplierRequest(
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

    private SupplierResponse createCompleteSupplierResponse() {
        return new SupplierResponse(
                UUID.randomUUID(),
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
                BigDecimal.valueOf(4.5),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}