package com.inventory.service;

import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.mapper.StockMovementMapper;
import com.inventory.repository.ProductRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementService Tests")
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockMovementMapper stockMovementMapper;

    @Mock
    private ProductRepository productRepository;

    private StockMovementService stockMovementService;

    @BeforeEach
    void setUp() {
        stockMovementService = new StockMovementService(stockMovementRepository, stockMovementMapper, productRepository);
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

    @Nested
    @DisplayName("createStockMovement() Tests")
    class CreateStockMovementTests {

        @Test
        @DisplayName("Should create IN stock movement successfully")
        void shouldCreateInStockMovementSuccessfully() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.IN, 10, MovementReason.PURCHASE,
                    "PO-2024-001", "Restocking inventory"
            );

            Product product = createProductWithStock(15);
            StockMovement savedMovement = createStockMovementForProduct(product, MovementType.IN, 10, 15, 25);
            StockMovementResponse expectedResponse = createStockMovementResponse();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(stockMovementRepository.save(any(StockMovement.class))).willReturn(savedMovement);
            given(productRepository.save(product)).willReturn(product);
            given(stockMovementMapper.toResponse(savedMovement)).willReturn(expectedResponse);

            // When
            StockMovementResponse result = stockMovementService.createStockMovement(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(product.getStockQuantity()).isEqualTo(25);
            then(productRepository).should().findById(productId);
            then(productRepository).should().save(product);
            then(stockMovementRepository).should().save(any(StockMovement.class));
            then(stockMovementMapper).should().toResponse(savedMovement);
        }

        @Test
        @DisplayName("Should create OUT stock movement successfully")
        void shouldCreateOutStockMovementSuccessfully() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.OUT, 5, MovementReason.SALE,
                    "ORDER-123", "Customer order"
            );

            Product product = createProductWithStock(20);
            StockMovement savedMovement = createStockMovementForProduct(product, MovementType.OUT, 5, 20, 15);
            StockMovementResponse expectedResponse = createStockMovementResponse();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(stockMovementRepository.save(any(StockMovement.class))).willReturn(savedMovement);
            given(productRepository.save(product)).willReturn(product);
            given(stockMovementMapper.toResponse(savedMovement)).willReturn(expectedResponse);

            // When
            StockMovementResponse result = stockMovementService.createStockMovement(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(product.getStockQuantity()).isEqualTo(15);
            then(productRepository).should().findById(productId);
            then(productRepository).should().save(product);
            then(stockMovementRepository).should().save(any(StockMovement.class));
            then(stockMovementMapper).should().toResponse(savedMovement);
        }

        @Test
        @DisplayName("Should handle product with null stock quantity as zero")
        void shouldHandleProductWithNullStockQuantityAsZero() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.IN, 10, MovementReason.ADJUSTMENT,
                    "ADJ-001", "Initial stock adjustment"
            );

            Product product = createProductWithStock(null);
            StockMovement savedMovement = createStockMovementForProduct(product, MovementType.IN, 10, 0, 10);
            StockMovementResponse expectedResponse = createStockMovementResponse();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(stockMovementRepository.save(any(StockMovement.class))).willReturn(savedMovement);
            given(productRepository.save(product)).willReturn(product);
            given(stockMovementMapper.toResponse(savedMovement)).willReturn(expectedResponse);

            // When
            StockMovementResponse result = stockMovementService.createStockMovement(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(product.getStockQuantity()).isEqualTo(10);
            then(productRepository).should().findById(productId);
            then(productRepository).should().save(product);
            then(stockMovementRepository).should().save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product does not exist")
        void shouldThrowProductNotFoundExceptionWhenProductDoesNotExist() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.IN, 10, MovementReason.PURCHASE,
                    "PO-2024-001", "Restocking inventory"
            );

            given(productRepository.findById(productId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> stockMovementService.createStockMovement(request))
                    .isInstanceOf(ProductNotFoundException.class);

            then(productRepository).should().findById(productId);
            then(stockMovementRepository).should(never()).save(any());
            then(productRepository).should(never()).save(any());
            then(stockMovementMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product is inactive")
        void shouldThrowProductNotFoundExceptionWhenProductIsInactive() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.IN, 10, MovementReason.PURCHASE,
                    "PO-2024-001", "Restocking inventory"
            );

            Product inactiveProduct = createProductWithStock(15);
            inactiveProduct.softDelete();

            given(productRepository.findById(productId)).willReturn(Optional.of(inactiveProduct));

            // When & Then
            assertThatThrownBy(() -> stockMovementService.createStockMovement(request))
                    .isInstanceOf(ProductNotFoundException.class);

            then(productRepository).should().findById(productId);
            then(stockMovementRepository).should(never()).save(any());
            then(productRepository).should(never()).save(any());
            then(stockMovementMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when OUT movement exceeds available stock")
        void shouldThrowInsufficientStockExceptionWhenOutMovementExceedsAvailableStock() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.OUT, 25, MovementReason.SALE,
                    "ORDER-123", "Customer order"
            );

            Product product = createProductWithStock(10);
            product.setSku("TEST-PRODUCT");

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            // When & Then
            assertThatThrownBy(() -> stockMovementService.createStockMovement(request))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock for product TEST-PRODUCT")
                    .hasMessageContaining("Current stock: 10")
                    .hasMessageContaining("requested: 25");

            then(productRepository).should().findById(productId);
            then(stockMovementRepository).should(never()).save(any());
            then(productRepository).should(never()).save(any());
            then(stockMovementMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when OUT movement from zero stock")
        void shouldThrowInsufficientStockExceptionWhenOutMovementFromZeroStock() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.OUT, 1, MovementReason.SALE,
                    "ORDER-123", "Customer order"
            );

            Product product = createProductWithStock(null);
            product.setSku("ZERO-STOCK");

            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            // When & Then
            assertThatThrownBy(() -> stockMovementService.createStockMovement(request))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock for product ZERO-STOCK")
                    .hasMessageContaining("Current stock: 0")
                    .hasMessageContaining("requested: 1");

            then(productRepository).should().findById(productId);
            then(stockMovementRepository).should(never()).save(any());
            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should allow OUT movement that results in exactly zero stock")
        void shouldAllowOutMovementThatResultsInExactlyZeroStock() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.OUT, 10, MovementReason.SALE,
                    "ORDER-123", "Final sale"
            );

            Product product = createProductWithStock(10);
            StockMovement savedMovement = createStockMovementForProduct(product, MovementType.OUT, 10, 10, 0);
            StockMovementResponse expectedResponse = createStockMovementResponse();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(stockMovementRepository.save(any(StockMovement.class))).willReturn(savedMovement);
            given(productRepository.save(product)).willReturn(product);
            given(stockMovementMapper.toResponse(savedMovement)).willReturn(expectedResponse);

            // When
            StockMovementResponse result = stockMovementService.createStockMovement(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(product.getStockQuantity()).isEqualTo(0);
            then(productRepository).should().findById(productId);
            then(productRepository).should().save(product);
            then(stockMovementRepository).should().save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Should handle different movement reasons correctly")
        void shouldHandleDifferentMovementReasonsCorrectly() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest adjustmentRequest = new CreateStockMovementRequest(
                    productId, MovementType.IN, 5, MovementReason.ADJUSTMENT,
                    "ADJ-001", "Stock count adjustment"
            );

            Product product = createProductWithStock(10);
            StockMovement savedMovement = createStockMovementForProduct(product, MovementType.IN, 5, 10, 15);
            savedMovement.setReason(MovementReason.ADJUSTMENT);
            StockMovementResponse expectedResponse = createStockMovementResponse();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(stockMovementRepository.save(any(StockMovement.class))).willReturn(savedMovement);
            given(productRepository.save(product)).willReturn(product);
            given(stockMovementMapper.toResponse(savedMovement)).willReturn(expectedResponse);

            // When
            StockMovementResponse result = stockMovementService.createStockMovement(adjustmentRequest);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(productRepository).should().findById(productId);
            then(stockMovementRepository).should().save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Should handle return movement type correctly")
        void shouldHandleReturnMovementTypeCorrectly() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest returnRequest = new CreateStockMovementRequest(
                    productId, MovementType.IN, 3, MovementReason.RETURN,
                    "RET-456", "Customer return"
            );

            Product product = createProductWithStock(12);
            StockMovement savedMovement = createStockMovementForProduct(product, MovementType.IN, 3, 12, 15);
            savedMovement.setReason(MovementReason.RETURN);
            StockMovementResponse expectedResponse = createStockMovementResponse();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(stockMovementRepository.save(any(StockMovement.class))).willReturn(savedMovement);
            given(productRepository.save(product)).willReturn(product);
            given(stockMovementMapper.toResponse(savedMovement)).willReturn(expectedResponse);

            // When
            StockMovementResponse result = stockMovementService.createStockMovement(returnRequest);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(product.getStockQuantity()).isEqualTo(15);
            then(productRepository).should().findById(productId);
            then(stockMovementRepository).should().save(any(StockMovement.class));
        }

        @Test
        @DisplayName("Should create stock movement with optional fields as null")
        void shouldCreateStockMovementWithOptionalFieldsAsNull() {
            // Given
            UUID productId = UUID.randomUUID();
            CreateStockMovementRequest request = new CreateStockMovementRequest(
                    productId, MovementType.IN, 8, MovementReason.PURCHASE,
                    null, null // reference and notes are null
            );

            Product product = createProductWithStock(7);
            StockMovement savedMovement = createStockMovementForProduct(product, MovementType.IN, 8, 7, 15);
            savedMovement.setReference(null);
            savedMovement.setNotes(null);
            StockMovementResponse expectedResponse = createStockMovementResponse();

            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(stockMovementRepository.save(any(StockMovement.class))).willReturn(savedMovement);
            given(productRepository.save(product)).willReturn(product);
            given(stockMovementMapper.toResponse(savedMovement)).willReturn(expectedResponse);

            // When
            StockMovementResponse result = stockMovementService.createStockMovement(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(product.getStockQuantity()).isEqualTo(15);
            then(productRepository).should().findById(productId);
            then(stockMovementRepository).should().save(any(StockMovement.class));
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

    private Product createProductWithStock(Integer stockQuantity) {
        Product product = createProduct();
        product.setStockQuantity(stockQuantity);
        return product;
    }

    private StockMovement createStockMovementForProduct(Product product, MovementType movementType, 
                                                        Integer quantity, Integer previousStock, Integer newStock) {
        StockMovement stockMovement = new StockMovement();
        stockMovement.setId(UUID.randomUUID());
        stockMovement.setProduct(product);
        stockMovement.setMovementType(movementType);
        stockMovement.setQuantity(quantity);
        stockMovement.setPreviousStock(previousStock);
        stockMovement.setNewStock(newStock);
        stockMovement.setReason(MovementReason.PURCHASE);
        stockMovement.setReference("REF-001");
        stockMovement.setNotes("Test movement");
        stockMovement.setCreatedBy("system");
        return stockMovement;
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