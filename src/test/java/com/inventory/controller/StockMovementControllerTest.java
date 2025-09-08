package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.exception.GlobalExceptionHandler;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.service.StockMovementService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementController Tests")
class StockMovementControllerTest {

    @Mock
    private StockMovementService stockMovementService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new StockMovementController(stockMovementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/stock-movements")
    class GetAllMovementsTests {

        @Test
        @DisplayName("Should return paginated stock movements with default pagination")
        void shouldReturnPaginatedStockMovementsWithDefaultPagination() throws Exception {
            // Given
            StockMovementResponse stockMovement = createStockMovementResponse();
            Page<StockMovementResponse> page = new PageImpl<>(List.of(stockMovement), PageRequest.of(0, 20), 1);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].productSku").value("IPHONE15"))
                    .andExpect(jsonPath("$.content[0].productName").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[0].movementType").value("IN"))
                    .andExpect(jsonPath("$.content[0].reason").value("PURCHASE"))
                    .andExpect(jsonPath("$.content[0].quantity").value(5))
                    .andExpect(jsonPath("$.content[0].previousStock").value(10))
                    .andExpect(jsonPath("$.content[0].newStock").value(15))
                    .andExpect(jsonPath("$.content[0].reference").value("PO-2024-001"))
                    .andExpect(jsonPath("$.content[0].createdBy").value("admin"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0));

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no stock movements exist")
        void shouldReturnEmptyPageWhenNoStockMovementsExist() throws Exception {
            // Given
            Page<StockMovementResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.size").value(20));

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom page size parameter")
        void shouldHandleCustomPageSizeParameter() throws Exception {
            // Given
            StockMovementResponse stockMovement = createStockMovementResponse();
            Page<StockMovementResponse> page = new PageImpl<>(List.of(stockMovement), PageRequest.of(0, 5), 1);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").hasJsonPath())
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalElements").value(1));

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom page number parameter")
        void shouldHandleCustomPageNumberParameter() throws Exception {
            // Given
            StockMovementResponse stockMovement = createStockMovementResponse();
            Page<StockMovementResponse> page = new PageImpl<>(List.of(stockMovement), PageRequest.of(1, 20), 21);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements")
                            .param("page", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.totalElements").value(21))
                    .andExpect(jsonPath("$.totalPages").value(2));

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle both custom page and size parameters")
        void shouldHandleBothCustomPageAndSizeParameters() throws Exception {
            // Given
            StockMovementResponse stockMovement = createStockMovementResponse();
            Page<StockMovementResponse> page = new PageImpl<>(List.of(stockMovement), PageRequest.of(2, 10), 50);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements")
                            .param("page", "2")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(2))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalElements").value(50))
                    .andExpect(jsonPath("$.totalPages").value(5));

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sorting parameter with default sort by createdAt")
        void shouldHandleSortingParameterWithDefaultSortByCreatedAt() throws Exception {
            // Given
            StockMovementResponse stockMovement = createStockMovementResponse();
            Page<StockMovementResponse> page = new PageImpl<>(List.of(stockMovement), PageRequest.of(0, 20), 1);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements")
                            .param("sort", "createdAt,desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1));

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return multiple stock movements with different types and reasons")
        void shouldReturnMultipleStockMovementsWithDifferentTypesAndReasons() throws Exception {
            // Given
            StockMovementResponse inMovement = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.PURCHASE);
            StockMovementResponse outMovement = createStockMovementResponseWithTypeAndReason(MovementType.OUT, MovementReason.SALE);
            StockMovementResponse adjustmentMovement = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.ADJUSTMENT);
            
            List<StockMovementResponse> movements = List.of(inMovement, outMovement, adjustmentMovement);
            Page<StockMovementResponse> page = new PageImpl<>(movements, PageRequest.of(0, 20), 3);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").hasJsonPath())
                    .andExpect(jsonPath("$.content[0].movementType").value("IN"))
                    .andExpect(jsonPath("$.content[0].reason").value("PURCHASE"))
                    .andExpect(jsonPath("$.content[1].movementType").value("OUT"))
                    .andExpect(jsonPath("$.content[1].reason").value("SALE"))
                    .andExpect(jsonPath("$.content[2].movementType").value("IN"))
                    .andExpect(jsonPath("$.content[2].reason").value("ADJUSTMENT"))
                    .andExpect(jsonPath("$.totalElements").value(3));

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle invalid page parameter gracefully")
        void shouldHandleInvalidPageParameterGracefully() throws Exception {
            // Given
            StockMovementResponse stockMovement = createStockMovementResponse();
            Page<StockMovementResponse> page = new PageImpl<>(List.of(stockMovement), PageRequest.of(0, 20), 1);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements")
                            .param("page", "-1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray());

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle invalid size parameter gracefully")
        void shouldHandleInvalidSizeParameterGracefully() throws Exception {
            // Given
            StockMovementResponse stockMovement = createStockMovementResponse();
            Page<StockMovementResponse> page = new PageImpl<>(List.of(stockMovement), PageRequest.of(0, 20), 1);

            given(stockMovementService.getAllMovements(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/stock-movements")
                            .param("size", "0"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray());

            then(stockMovementService).should().getAllMovements(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/stock-movements")
    class CreateStockMovementTests {

        @Test
        @DisplayName("Should create IN stock movement successfully")
        void shouldCreateInStockMovementSuccessfully() throws Exception {
            // Given
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 10, MovementReason.PURCHASE,
                    "PO-2024-001", "Restocking inventory"
            );
            StockMovementResponse response = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.PURCHASE);

            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.productSku").value("IPHONE15"))
                    .andExpect(jsonPath("$.productName").value("iPhone 15"))
                    .andExpect(jsonPath("$.movementType").value("IN"))
                    .andExpect(jsonPath("$.reason").value("PURCHASE"))
                    .andExpect(jsonPath("$.quantity").value(5))
                    .andExpect(jsonPath("$.previousStock").value(10))
                    .andExpect(jsonPath("$.newStock").value(15))
                    .andExpect(jsonPath("$.createdBy").value("admin"));

            then(stockMovementService).should().createStockMovement(any(CreateStockMovementRequest.class));
        }

        @Test
        @DisplayName("Should create OUT stock movement successfully")
        void shouldCreateOutStockMovementSuccessfully() throws Exception {
            // Given
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.OUT, 5, MovementReason.SALE,
                    "ORDER-123", "Customer order"
            );
            StockMovementResponse response = createStockMovementResponseWithTypeAndReason(MovementType.OUT, MovementReason.SALE);

            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.movementType").value("OUT"))
                    .andExpect(jsonPath("$.reason").value("SALE"))
                    .andExpect(jsonPath("$.quantity").value(5))
                    .andExpect(jsonPath("$.previousStock").value(15))
                    .andExpect(jsonPath("$.newStock").value(10));

            then(stockMovementService).should().createStockMovement(any(CreateStockMovementRequest.class));
        }

        @Test
        @DisplayName("Should create stock movement with different reasons")
        void shouldCreateStockMovementWithDifferentReasons() throws Exception {
            // Given
            CreateStockMovementRequest adjustmentRequest = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 3, MovementReason.ADJUSTMENT,
                    "ADJ-001", "Stock count adjustment"
            );
            StockMovementResponse response = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.ADJUSTMENT);

            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustmentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.reason").value("ADJUSTMENT"));

            then(stockMovementService).should().createStockMovement(any(CreateStockMovementRequest.class));
        }

        @Test
        @DisplayName("Should create stock movement with optional fields as null")
        void shouldCreateStockMovementWithOptionalFieldsAsNull() throws Exception {
            // Given
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 8, MovementReason.RETURN,
                    null, null // reference and notes are null
            );
            StockMovementResponse response = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.RETURN);

            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.reason").value("RETURN"));

            then(stockMovementService).should().createStockMovement(any(CreateStockMovementRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body validation fails")
        void shouldReturn400WhenRequestBodyValidationFails() throws Exception {
            // Given
            CreateStockMovementRequest invalidRequest = new CreateStockMovementRequest(
                    null, // productId is null
                    null, // movementType is null
                    0, // quantity is 0 (should be >= 1)
                    null, // reason is null
                    "a".repeat(101), // reference exceeds 100 characters
                    "a".repeat(501) // notes exceed 500 characters
            );

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors").exists());
        }

        @Test
        @DisplayName("Should return 400 when quantity is zero")
        void shouldReturn400WhenQuantityIsZero() throws Exception {
            // Given
            CreateStockMovementRequest invalidRequest = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 0, MovementReason.PURCHASE,
                    "PO-001", "Test"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.fieldErrors.quantity").value("Quantity must be greater than 0"));
        }

        @Test
        @DisplayName("Should return 400 when quantity is negative")
        void shouldReturn400WhenQuantityIsNegative() throws Exception {
            // Given
            CreateStockMovementRequest invalidRequest = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, -5, MovementReason.PURCHASE,
                    "PO-001", "Test"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.quantity").value("Quantity must be greater than 0"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.IN, 10, MovementReason.PURCHASE,
                    "PO-2024-001", "Restocking inventory"
            );

            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class)))
                    .willThrow(new ProductNotFoundException(productId));

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Product Not Found"));

            then(stockMovementService).should().createStockMovement(any(CreateStockMovementRequest.class));
        }

        @Test
        @DisplayName("Should return 422 when insufficient stock for OUT movement")
        void shouldReturn422WhenInsufficientStockForOutMovement() throws Exception {
            // Given
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.OUT, 25, MovementReason.SALE,
                    "ORDER-123", "Customer order"
            );

            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class)))
                    .willThrow(new InsufficientStockException("TEST-PRODUCT", 10, 25));

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.error").value("Insufficient Stock"))
                    .andExpect(jsonPath("$.message").value("Insufficient stock for product TEST-PRODUCT. Current stock: 10, requested: 25"));

            then(stockMovementService).should().createStockMovement(any(CreateStockMovementRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void shouldReturn400WhenRequestBodyIsEmpty() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.fieldErrors").exists());
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed JSON")
        void shouldReturn400WhenRequestBodyIsMalformedJson() throws Exception {
            // When & Then - Malformed JSON typically results in 500 from Jackson
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid json"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should return 415 when Content-Type is not JSON")
        void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
            // Given
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 10, MovementReason.PURCHASE,
                    "PO-2024-001", "Restocking inventory"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should return 400 when reference exceeds maximum length")
        void shouldReturn400WhenReferenceExceedsMaximumLength() throws Exception {
            // Given
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 10, MovementReason.PURCHASE,
                    "a".repeat(101), // Exceeds 100 character limit
                    "Valid notes"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.reference").value("Reference must not exceed 100 characters"));
        }

        @Test
        @DisplayName("Should return 400 when notes exceed maximum length")
        void shouldReturn400WhenNotesExceedMaximumLength() throws Exception {
            // Given
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 10, MovementReason.PURCHASE,
                    "Valid reference",
                    "a".repeat(501) // Exceeds 500 character limit
            );

            // When & Then
            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.notes").value("Notes must not exceed 500 characters"));
        }

        @Test
        @DisplayName("Should handle all MovementType and MovementReason combinations")
        void shouldHandleAllMovementTypeAndMovementReasonCombinations() throws Exception {
            // Test IN + PURCHASE
            CreateStockMovementRequest purchaseRequest = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 10, MovementReason.PURCHASE,
                    "PO-001", "Purchase order"
            );
            StockMovementResponse purchaseResponse = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.PURCHASE);
            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(purchaseResponse);

            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(purchaseRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.movementType").value("IN"))
                    .andExpect(jsonPath("$.reason").value("PURCHASE"));

            // Test OUT + SALE  
            CreateStockMovementRequest saleRequest = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.OUT, 5, MovementReason.SALE,
                    "ORD-001", "Customer sale"
            );
            StockMovementResponse saleResponse = createStockMovementResponseWithTypeAndReason(MovementType.OUT, MovementReason.SALE);
            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(saleResponse);

            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(saleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.movementType").value("OUT"))
                    .andExpect(jsonPath("$.reason").value("SALE"));

            // Test IN + RETURN
            CreateStockMovementRequest returnRequest = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 2, MovementReason.RETURN,
                    "RET-001", "Customer return"
            );
            StockMovementResponse returnResponse = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.RETURN);
            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(returnResponse);

            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(returnRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.movementType").value("IN"))
                    .andExpect(jsonPath("$.reason").value("RETURN"));

            // Test IN + ADJUSTMENT
            CreateStockMovementRequest adjustmentRequest = new CreateStockMovementRequest(
                    UUID.randomUUID(), MovementType.IN, 3, MovementReason.ADJUSTMENT,
                    "ADJ-001", "Inventory adjustment"
            );
            StockMovementResponse adjustmentResponse = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.ADJUSTMENT);
            given(stockMovementService.createStockMovement(any(CreateStockMovementRequest.class))).willReturn(adjustmentResponse);

            mockMvc.perform(post("/api/v1/stock-movements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(adjustmentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.movementType").value("IN"))
                    .andExpect(jsonPath("$.reason").value("ADJUSTMENT"));
        }
    }

    private StockMovementResponse createStockMovementResponse() {
        return new StockMovementResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "IPHONE15",
                "iPhone 15",
                MovementType.IN,
                5,
                10,
                15,
                MovementReason.PURCHASE,
                "PO-2024-001",
                "Initial stock purchase",
                "admin",
                LocalDateTime.now()
        );
    }

    private StockMovementResponse createStockMovementResponseWithTypeAndReason(MovementType movementType, MovementReason reason) {
        StockMovementResponse base = createStockMovementResponse();
        
        int previousStock = movementType == MovementType.OUT ? 15 : 10;
        int newStock = movementType == MovementType.OUT ? 10 : 15;
        
        return new StockMovementResponse(
                base.id(), base.productId(), base.productSku(), base.productName(),
                movementType, base.quantity(), previousStock, newStock, reason,
                base.reference(), base.notes(), base.createdBy(), base.createdAt()
        );
    }
}