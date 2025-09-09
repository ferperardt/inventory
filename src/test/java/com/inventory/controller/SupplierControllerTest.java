package com.inventory.controller;

import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Address;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
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

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new SupplierController(supplierService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
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