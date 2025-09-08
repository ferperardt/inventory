package com.inventory.mapper;

import com.inventory.dto.response.StockMovementResponse;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("StockMovementMapper Tests")
class StockMovementMapperTest {

    @Autowired
    private StockMovementMapper stockMovementMapper;

    @Nested
    @DisplayName("toResponse() Tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should map StockMovement to StockMovementResponse successfully")
        void shouldMapStockMovementToStockMovementResponseSuccessfully() {
            // Given
            StockMovement stockMovement = createCompleteStockMovement();

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(stockMovement.getId());
            assertThat(result.productId()).isEqualTo(stockMovement.getProduct().getId());
            assertThat(result.productSku()).isEqualTo(stockMovement.getProduct().getSku());
            assertThat(result.productName()).isEqualTo(stockMovement.getProduct().getName());
            assertThat(result.movementType()).isEqualTo(stockMovement.getMovementType());
            assertThat(result.quantity()).isEqualTo(stockMovement.getQuantity());
            assertThat(result.previousStock()).isEqualTo(stockMovement.getPreviousStock());
            assertThat(result.newStock()).isEqualTo(stockMovement.getNewStock());
            assertThat(result.reason()).isEqualTo(stockMovement.getReason());
            assertThat(result.reference()).isEqualTo(stockMovement.getReference());
            assertThat(result.notes()).isEqualTo(stockMovement.getNotes());
            assertThat(result.createdBy()).isEqualTo(stockMovement.getCreatedBy());
            assertThat(result.createdAt()).isEqualTo(stockMovement.getCreatedAt());
        }

        @Test
        @DisplayName("Should map StockMovement with IN movement type")
        void shouldMapStockMovementWithInMovementType() {
            // Given
            StockMovement stockMovement = createCompleteStockMovement();
            stockMovement.setMovementType(MovementType.IN);
            stockMovement.setReason(MovementReason.PURCHASE);

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result.movementType()).isEqualTo(MovementType.IN);
            assertThat(result.reason()).isEqualTo(MovementReason.PURCHASE);
        }

        @Test
        @DisplayName("Should map StockMovement with OUT movement type")
        void shouldMapStockMovementWithOutMovementType() {
            // Given
            StockMovement stockMovement = createCompleteStockMovement();
            stockMovement.setMovementType(MovementType.OUT);
            stockMovement.setReason(MovementReason.SALE);

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result.movementType()).isEqualTo(MovementType.OUT);
            assertThat(result.reason()).isEqualTo(MovementReason.SALE);
        }

        @Test
        @DisplayName("Should map all movement reasons correctly")
        void shouldMapAllMovementReasonsCorrectly() {
            // Test PURCHASE
            StockMovement purchaseMovement = createCompleteStockMovement();
            purchaseMovement.setReason(MovementReason.PURCHASE);
            assertThat(stockMovementMapper.toResponse(purchaseMovement).reason()).isEqualTo(MovementReason.PURCHASE);

            // Test SALE
            StockMovement saleMovement = createCompleteStockMovement();
            saleMovement.setReason(MovementReason.SALE);
            assertThat(stockMovementMapper.toResponse(saleMovement).reason()).isEqualTo(MovementReason.SALE);

            // Test ADJUSTMENT
            StockMovement adjustmentMovement = createCompleteStockMovement();
            adjustmentMovement.setReason(MovementReason.ADJUSTMENT);
            assertThat(stockMovementMapper.toResponse(adjustmentMovement).reason()).isEqualTo(MovementReason.ADJUSTMENT);

            // Test RETURN
            StockMovement returnMovement = createCompleteStockMovement();
            returnMovement.setReason(MovementReason.RETURN);
            assertThat(stockMovementMapper.toResponse(returnMovement).reason()).isEqualTo(MovementReason.RETURN);
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            // Given
            StockMovement stockMovement = createMinimalStockMovement();
            stockMovement.setReference(null);
            stockMovement.setNotes(null);

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.reference()).isNull();
            assertThat(result.notes()).isNull();
            assertThat(result.id()).isEqualTo(stockMovement.getId());
            assertThat(result.productId()).isEqualTo(stockMovement.getProduct().getId());
        }

        @Test
        @DisplayName("Should handle empty string optional fields")
        void shouldHandleEmptyStringOptionalFields() {
            // Given
            StockMovement stockMovement = createCompleteStockMovement();
            stockMovement.setReference("");
            stockMovement.setNotes("");

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result.reference()).isEmpty();
            assertThat(result.notes()).isEmpty();
        }

        @Test
        @DisplayName("Should map product relationship correctly")
        void shouldMapProductRelationshipCorrectly() {
            // Given
            Product product = createProduct();
            product.setId(UUID.randomUUID());
            product.setSku("PROD-123");
            product.setName("Test Product");

            StockMovement stockMovement = createMinimalStockMovement();
            stockMovement.setProduct(product);

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.productSku()).isEqualTo("PROD-123");
            assertThat(result.productName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Should handle product with null optional fields")
        void shouldHandleProductWithNullOptionalFields() {
            // Given
            Product product = new Product();
            product.setId(UUID.randomUUID());
            product.setSku("PROD-456");
            product.setName("Minimal Product");
            // other fields are null

            StockMovement stockMovement = createMinimalStockMovement();
            stockMovement.setProduct(product);

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result.productId()).isEqualTo(product.getId());
            assertThat(result.productSku()).isEqualTo("PROD-456");
            assertThat(result.productName()).isEqualTo("Minimal Product");
        }

        @Test
        @DisplayName("Should return null when StockMovement is null")
        void shouldReturnNullWhenStockMovementIsNull() {
            // When
            StockMovementResponse result = stockMovementMapper.toResponse(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle zero quantities")
        void shouldHandleZeroQuantities() {
            // Given
            StockMovement stockMovement = createCompleteStockMovement();
            stockMovement.setQuantity(0);
            stockMovement.setPreviousStock(0);
            stockMovement.setNewStock(0);

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result.quantity()).isZero();
            assertThat(result.previousStock()).isZero();
            assertThat(result.newStock()).isZero();
        }

        @Test
        @DisplayName("Should handle large quantities")
        void shouldHandleLargeQuantities() {
            // Given
            StockMovement stockMovement = createCompleteStockMovement();
            stockMovement.setQuantity(999999);
            stockMovement.setPreviousStock(500000);
            stockMovement.setNewStock(1499999);

            // When
            StockMovementResponse result = stockMovementMapper.toResponse(stockMovement);

            // Then
            assertThat(result.quantity()).isEqualTo(999999);
            assertThat(result.previousStock()).isEqualTo(500000);
            assertThat(result.newStock()).isEqualTo(1499999);
        }

    }

    private StockMovement createCompleteStockMovement() {
        StockMovement stockMovement = new StockMovement();
        stockMovement.setId(UUID.randomUUID());
        stockMovement.setProduct(createProduct());
        stockMovement.setMovementType(MovementType.IN);
        stockMovement.setQuantity(10);
        stockMovement.setPreviousStock(5);
        stockMovement.setNewStock(15);
        stockMovement.setReason(MovementReason.PURCHASE);
        stockMovement.setReference("PO-2024-001");
        stockMovement.setNotes("Purchase from supplier ABC");
        stockMovement.setCreatedBy("admin@company.com");
        // createdAt is automatically managed by @CreatedDate
        return stockMovement;
    }

    private StockMovement createMinimalStockMovement() {
        StockMovement stockMovement = new StockMovement();
        stockMovement.setId(UUID.randomUUID());
        stockMovement.setProduct(createProduct());
        stockMovement.setMovementType(MovementType.OUT);
        stockMovement.setQuantity(1);
        stockMovement.setPreviousStock(10);
        stockMovement.setNewStock(9);
        stockMovement.setReason(MovementReason.SALE);
        stockMovement.setCreatedBy("user@company.com");
        // createdAt is automatically managed by @CreatedDate
        return stockMovement;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("iPhone 15");
        product.setSku("IPHONE15");
        product.setDescription("Latest iPhone model");
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setStockQuantity(10);
        product.setMinStockLevel(5);
        product.setCategory("electronics");
        return product;
    }
}