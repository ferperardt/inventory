package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.exception.DuplicateBusinessIdException;
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