package com.inventory.controller;

import com.inventory.dto.response.StockMovementResponse;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.exception.GlobalExceptionHandler;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementController Tests")
class StockMovementControllerTest {

    @Mock
    private StockMovementService stockMovementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
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