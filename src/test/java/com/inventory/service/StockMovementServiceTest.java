package com.inventory.service;

import com.inventory.dto.response.StockMovementResponse;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.mapper.StockMovementMapper;
import com.inventory.repository.StockMovementRepository;
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
@DisplayName("StockMovementService Tests")
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockMovementMapper stockMovementMapper;

    private StockMovementService stockMovementService;

    @BeforeEach
    void setUp() {
        stockMovementService = new StockMovementService(stockMovementRepository, stockMovementMapper);
    }

    @Nested
    @DisplayName("getAllMovements() Tests")
    class GetAllMovementsTests {

        @Test
        @DisplayName("Should return paginated active stock movements ordered by created date desc")
        void shouldReturnPaginatedActiveStockMovementsOrderedByCreatedDateDesc() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            StockMovement stockMovement = createStockMovement();
            StockMovementResponse stockMovementResponse = createStockMovementResponse();
            Page<StockMovement> stockMovementPage = new PageImpl<>(List.of(stockMovement), pageable, 1);

            given(stockMovementRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)).willReturn(stockMovementPage);
            given(stockMovementMapper.toResponse(stockMovement)).willReturn(stockMovementResponse);

            // When
            Page<StockMovementResponse> result = stockMovementService.getAllMovements(pageable);

            // Then
            assertThat(result.getContent()).containsExactly(stockMovementResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getNumber()).isEqualTo(0);
            then(stockMovementRepository).should().findByActiveTrueOrderByCreatedAtDesc(pageable);
            then(stockMovementMapper).should().toResponse(stockMovement);
        }

        @Test
        @DisplayName("Should return empty page when no stock movements exist")
        void shouldReturnEmptyPageWhenNoStockMovementsExist() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<StockMovement> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(stockMovementRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)).willReturn(emptyPage);

            // When
            Page<StockMovementResponse> result = stockMovementService.getAllMovements(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            then(stockMovementRepository).should().findByActiveTrueOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("Should return paginated stock movements with multiple pages")
        void shouldReturnPaginatedStockMovementsWithMultiplePages() {
            // Given
            Pageable pageable = PageRequest.of(1, 5);
            StockMovement inMovement = createStockMovement();
            StockMovement outMovement = createStockMovement();
            outMovement.setMovementType(MovementType.OUT);
            outMovement.setReason(MovementReason.SALE);
            
            List<StockMovement> movements = List.of(inMovement, outMovement);
            Page<StockMovement> stockMovementPage = new PageImpl<>(movements, pageable, 15);

            StockMovementResponse inResponse = createStockMovementResponse();
            StockMovementResponse outResponse = createStockMovementResponse();
            outResponse = new StockMovementResponse(
                    outResponse.id(), outResponse.productId(), outResponse.productSku(),
                    outResponse.productName(), MovementType.OUT, outResponse.quantity(),
                    outResponse.previousStock(), outResponse.newStock(), MovementReason.SALE,
                    outResponse.reference(), outResponse.notes(), outResponse.createdBy(),
                    outResponse.createdAt()
            );

            given(stockMovementRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)).willReturn(stockMovementPage);
            given(stockMovementMapper.toResponse(inMovement)).willReturn(inResponse);
            given(stockMovementMapper.toResponse(outMovement)).willReturn(outResponse);

            // When
            Page<StockMovementResponse> result = stockMovementService.getAllMovements(pageable);

            // Then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).containsExactly(inResponse, outResponse);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            then(stockMovementRepository).should().findByActiveTrueOrderByCreatedAtDesc(pageable);
            then(stockMovementMapper).should().toResponse(inMovement);
            then(stockMovementMapper).should().toResponse(outMovement);
        }

        @Test
        @DisplayName("Should handle different page sizes correctly")
        void shouldHandleDifferentPageSizesCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 1);
            StockMovement stockMovement = createStockMovement();
            StockMovementResponse stockMovementResponse = createStockMovementResponse();
            Page<StockMovement> stockMovementPage = new PageImpl<>(List.of(stockMovement), pageable, 10);

            given(stockMovementRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)).willReturn(stockMovementPage);
            given(stockMovementMapper.toResponse(stockMovement)).willReturn(stockMovementResponse);

            // When
            Page<StockMovementResponse> result = stockMovementService.getAllMovements(pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(10);
            assertThat(result.getTotalPages()).isEqualTo(10);
            assertThat(result.getSize()).isEqualTo(1);
            then(stockMovementRepository).should().findByActiveTrueOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("Should handle stock movements with all movement types and reasons")
        void shouldHandleStockMovementsWithAllMovementTypesAndReasons() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            
            StockMovement purchaseMovement = createStockMovementWithTypeAndReason(MovementType.IN, MovementReason.PURCHASE);
            StockMovement saleMovement = createStockMovementWithTypeAndReason(MovementType.OUT, MovementReason.SALE);
            StockMovement adjustmentMovement = createStockMovementWithTypeAndReason(MovementType.IN, MovementReason.ADJUSTMENT);
            StockMovement returnMovement = createStockMovementWithTypeAndReason(MovementType.IN, MovementReason.RETURN);
            
            List<StockMovement> movements = List.of(purchaseMovement, saleMovement, adjustmentMovement, returnMovement);
            Page<StockMovement> stockMovementPage = new PageImpl<>(movements, pageable, 4);

            StockMovementResponse purchaseResponse = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.PURCHASE);
            StockMovementResponse saleResponse = createStockMovementResponseWithTypeAndReason(MovementType.OUT, MovementReason.SALE);
            StockMovementResponse adjustmentResponse = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.ADJUSTMENT);
            StockMovementResponse returnResponse = createStockMovementResponseWithTypeAndReason(MovementType.IN, MovementReason.RETURN);

            given(stockMovementRepository.findByActiveTrueOrderByCreatedAtDesc(pageable)).willReturn(stockMovementPage);
            given(stockMovementMapper.toResponse(purchaseMovement)).willReturn(purchaseResponse);
            given(stockMovementMapper.toResponse(saleMovement)).willReturn(saleResponse);
            given(stockMovementMapper.toResponse(adjustmentMovement)).willReturn(adjustmentResponse);
            given(stockMovementMapper.toResponse(returnMovement)).willReturn(returnResponse);

            // When
            Page<StockMovementResponse> result = stockMovementService.getAllMovements(pageable);

            // Then
            assertThat(result.getContent()).hasSize(4);
            assertThat(result.getContent().get(0).movementType()).isEqualTo(MovementType.IN);
            assertThat(result.getContent().get(0).reason()).isEqualTo(MovementReason.PURCHASE);
            assertThat(result.getContent().get(1).movementType()).isEqualTo(MovementType.OUT);
            assertThat(result.getContent().get(1).reason()).isEqualTo(MovementReason.SALE);
            assertThat(result.getContent().get(2).movementType()).isEqualTo(MovementType.IN);
            assertThat(result.getContent().get(2).reason()).isEqualTo(MovementReason.ADJUSTMENT);
            assertThat(result.getContent().get(3).movementType()).isEqualTo(MovementType.IN);
            assertThat(result.getContent().get(3).reason()).isEqualTo(MovementReason.RETURN);
            then(stockMovementRepository).should().findByActiveTrueOrderByCreatedAtDesc(pageable);
        }
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("iPhone 15");
        product.setDescription("Latest iPhone");
        product.setSku("IPHONE15");
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setStockQuantity(10);
        product.setMinStockLevel(5);
        product.setCategory("electronics");
        return product;
    }

    private StockMovement createStockMovement() {
        Product product = createProduct();
        StockMovement stockMovement = new StockMovement();
        stockMovement.setId(UUID.randomUUID());
        stockMovement.setProduct(product);
        stockMovement.setMovementType(MovementType.IN);
        stockMovement.setQuantity(5);
        stockMovement.setPreviousStock(10);
        stockMovement.setNewStock(15);
        stockMovement.setReason(MovementReason.PURCHASE);
        stockMovement.setReference("PO-2024-001");
        stockMovement.setNotes("Initial stock purchase");
        stockMovement.setCreatedBy("admin");
        return stockMovement;
    }

    private StockMovement createStockMovementWithTypeAndReason(MovementType movementType, MovementReason reason) {
        StockMovement stockMovement = createStockMovement();
        stockMovement.setMovementType(movementType);
        stockMovement.setReason(reason);
        
        if (movementType == MovementType.OUT) {
            stockMovement.setPreviousStock(15);
            stockMovement.setNewStock(10);
        }
        
        return stockMovement;
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