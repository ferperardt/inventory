package com.inventory.mapper;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProductMapper Tests")
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Nested
    @DisplayName("toResponse() Tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should map Product to ProductResponse successfully")
        void shouldMapProductToProductResponseSuccessfully() {
            // Given
            Product product = createCompleteProduct();

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(product.getId());
            assertThat(result.name()).isEqualTo(product.getName());
            assertThat(result.description()).isEqualTo(product.getDescription());
            assertThat(result.sku()).isEqualTo(product.getSku());
            assertThat(result.price()).isEqualTo(product.getPrice());
            assertThat(result.stockQuantity()).isEqualTo(product.getStockQuantity());
            assertThat(result.minStockLevel()).isEqualTo(product.getMinStockLevel());
            assertThat(result.category()).isEqualTo(product.getCategory());
            assertThat(result.active()).isEqualTo(product.getActive());
            assertThat(result.lowStock()).isFalse();
            assertThat(result.createdAt()).isEqualTo(product.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(product.getUpdatedAt());
        }

        @Test
        @DisplayName("Should use originalSku when available in sku mapping")
        void shouldUseOriginalSkuWhenAvailableInSkuMapping() {
            // Given
            Product product = createCompleteProduct();
            product.setOriginalSku("ORIGINAL-SKU-123");

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.sku()).isEqualTo("ORIGINAL-SKU-123");
        }

        @Test
        @DisplayName("Should use regular sku when originalSku is null")
        void shouldUseRegularSkuWhenOriginalSkuIsNull() {
            // Given
            Product product = createCompleteProduct();
            product.setOriginalSku(null);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.sku()).isEqualTo("IPHONE15");
        }

        @Test
        @DisplayName("Should use originalSku when it's an empty string")
        void shouldUseOriginalSkuWhenItsEmptyString() {
            // Given
            Product product = createCompleteProduct();
            product.setOriginalSku("");

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            // MapStruct expression only checks for null, not empty strings
            // So empty string originalSku will be used
            assertThat(result.sku()).isEmpty();
        }

        @Test
        @DisplayName("Should set lowStock to true when stock is below minimum level")
        void shouldSetLowStockToTrueWhenStockBelowMinimumLevel() {
            // Given
            Product product = createCompleteProduct();
            product.setStockQuantity(3);
            product.setMinStockLevel(5);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.lowStock()).isTrue();
        }

        @Test
        @DisplayName("Should set lowStock to true when stock equals minimum level")
        void shouldSetLowStockToTrueWhenStockEqualsMinimumLevel() {
            // Given
            Product product = createCompleteProduct();
            product.setStockQuantity(5);
            product.setMinStockLevel(5);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.lowStock()).isTrue();
        }

        @Test
        @DisplayName("Should set lowStock to false when stock is above minimum level")
        void shouldSetLowStockToFalseWhenStockAboveMinimumLevel() {
            // Given
            Product product = createCompleteProduct();
            product.setStockQuantity(10);
            product.setMinStockLevel(5);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.lowStock()).isFalse();
        }

        @Test
        @DisplayName("Should handle null stock quantity gracefully")
        void shouldHandleNullStockQuantityGracefully() {
            // Given
            Product product = createCompleteProduct();
            product.setStockQuantity(null);
            product.setMinStockLevel(5);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.lowStock()).isFalse();
            assertThat(result.stockQuantity()).isNull();
        }

        @Test
        @DisplayName("Should handle null minimum stock level gracefully")
        void shouldHandleNullMinimumStockLevelGracefully() {
            // Given
            Product product = createCompleteProduct();
            product.setStockQuantity(10);
            product.setMinStockLevel(null);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.lowStock()).isFalse();
            assertThat(result.minStockLevel()).isNull();
        }

        @Test
        @DisplayName("Should handle null product fields")
        void shouldHandleNullProductFields() {
            // Given
            Product product = new Product();
            product.setId(UUID.randomUUID());
            product.setName("Test Product");
            product.setSku("TEST-SKU");
            product.setPrice(BigDecimal.valueOf(100.00));

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(product.getId());
            assertThat(result.name()).isEqualTo("Test Product");
            assertThat(result.description()).isNull();
            assertThat(result.sku()).isEqualTo("TEST-SKU");
            assertThat(result.price()).isEqualTo(BigDecimal.valueOf(100.00));
            assertThat(result.stockQuantity()).isNull();
            assertThat(result.minStockLevel()).isNull();
            assertThat(result.category()).isNull();
            assertThat(result.lowStock()).isFalse();
        }

        @Test
        @DisplayName("Should return null when product is null")
        void shouldReturnNullWhenProductIsNull() {
            // When
            ProductResponse result = productMapper.toResponse(null);

            // Then
            assertThat(result).isNull();
        }


        @Test
        @DisplayName("Should handle zero stock quantity and minimum level")
        void shouldHandleZeroStockQuantityAndMinimumLevel() {
            // Given
            Product product = createCompleteProduct();
            product.setStockQuantity(0);
            product.setMinStockLevel(0);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.lowStock()).isTrue(); // 0 <= 0
            assertThat(result.stockQuantity()).isZero();
            assertThat(result.minStockLevel()).isZero();
        }

        @Test
        @DisplayName("Should handle both stock quantity and minimum level being null")
        void shouldHandleBothStockQuantityAndMinimumLevelBeingNull() {
            // Given
            Product product = createCompleteProduct();
            product.setStockQuantity(null);
            product.setMinStockLevel(null);

            // When
            ProductResponse result = productMapper.toResponse(product);

            // Then
            assertThat(result.lowStock()).isFalse();
            assertThat(result.stockQuantity()).isNull();
            assertThat(result.minStockLevel()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when CreateProductRequest is null")
        void shouldReturnNullWhenCreateProductRequestIsNull() {
            // When
            Product result = productMapper.toEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should map CreateProductRequest to Product entity successfully")
        void shouldMapCreateProductRequestToProductEntitySuccessfully() {
            // Given
            CreateProductRequest request = createCompleteCreateRequest();

            // When
            Product result = productMapper.toEntity(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull(); // Should be ignored
            assertThat(result.getName()).isEqualTo(request.name());
            assertThat(result.getDescription()).isEqualTo(request.description());
            assertThat(result.getSku()).isEqualTo(request.sku());
            assertThat(result.getPrice()).isEqualTo(request.price());
            assertThat(result.getStockQuantity()).isEqualTo(request.stockQuantity());
            assertThat(result.getMinStockLevel()).isEqualTo(request.minStockLevel());
            assertThat(result.getCategory()).isEqualTo(request.category());
            // BaseEntity has default value true for active field
            assertThat(result.getActive()).isTrue(); // Should be ignored but has default value
            assertThat(result.getOriginalSku()).isNull(); // Should be ignored
            assertThat(result.getCreatedAt()).isNull(); // Should be ignored
            assertThat(result.getUpdatedAt()).isNull(); // Should be ignored
            assertThat(result.getDeletedAt()).isNull(); // Should be ignored
        }

        @Test
        @DisplayName("Should handle CreateProductRequest with null optional fields")
        void shouldHandleCreateProductRequestWithNullOptionalFields() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "Test Product", null, "TEST-SKU",
                    BigDecimal.valueOf(100.00), null, null, null
            );

            // When
            Product result = productMapper.toEntity(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getSku()).isEqualTo("TEST-SKU");
            assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(100.00));
            assertThat(result.getStockQuantity()).isZero(); // Compact constructor default
            assertThat(result.getMinStockLevel()).isZero(); // Compact constructor default
            assertThat(result.getCategory()).isNull();
        }

        @Test
        @DisplayName("Should handle CreateProductRequest with empty strings")
        void shouldHandleCreateProductRequestWithEmptyStrings() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "Test Product", "", "TEST-SKU",
                    BigDecimal.valueOf(100.00), 10, 5, ""
            );

            // When
            Product result = productMapper.toEntity(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getDescription()).isEmpty();
            assertThat(result.getSku()).isEqualTo("TEST-SKU");
            assertThat(result.getCategory()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateProductFromRequest() Tests")
    class UpdateProductFromRequestTests {

        @Test
        @DisplayName("Should do nothing when UpdateProductRequest is null")
        void shouldDoNothingWhenUpdateProductRequestIsNull() {
            // Given
            Product existingProduct = createCompleteProduct();
            String originalName = existingProduct.getName();
            String originalDescription = existingProduct.getDescription();

            // When
            productMapper.updateProductFromRequest(null, existingProduct);

            // Then
            assertThat(existingProduct.getName()).isEqualTo(originalName);
            assertThat(existingProduct.getDescription()).isEqualTo(originalDescription);
        }

        @Test
        @DisplayName("Should update Product entity from UpdateProductRequest successfully")
        void shouldUpdateProductEntityFromUpdateProductRequestSuccessfully() {
            // Given
            Product existingProduct = createCompleteProduct();
            UUID originalId = existingProduct.getId();
            Boolean originalActive = existingProduct.getActive();
            String originalOriginalSku = existingProduct.getOriginalSku();
            LocalDateTime originalCreatedAt = existingProduct.getCreatedAt();
            LocalDateTime originalUpdatedAt = existingProduct.getUpdatedAt();
            LocalDateTime originalDeletedAt = existingProduct.getDeletedAt();

            UpdateProductRequest request = createCompleteUpdateRequest();

            // When
            productMapper.updateProductFromRequest(request, existingProduct);

            // Then
            assertThat(existingProduct.getId()).isEqualTo(originalId); // Should be ignored
            assertThat(existingProduct.getName()).isEqualTo(request.name());
            assertThat(existingProduct.getDescription()).isEqualTo(request.description());
            assertThat(existingProduct.getSku()).isEqualTo(request.sku());
            assertThat(existingProduct.getPrice()).isEqualTo(request.price());
            assertThat(existingProduct.getMinStockLevel()).isEqualTo(request.minStockLevel());
            assertThat(existingProduct.getCategory()).isEqualTo(request.category());
            assertThat(existingProduct.getActive()).isEqualTo(originalActive); // Should be ignored
            assertThat(existingProduct.getOriginalSku()).isEqualTo(originalOriginalSku); // Should be ignored
            assertThat(existingProduct.getCreatedAt()).isEqualTo(originalCreatedAt); // Should be ignored
            assertThat(existingProduct.getUpdatedAt()).isEqualTo(originalUpdatedAt); // Should be ignored
            assertThat(existingProduct.getDeletedAt()).isEqualTo(originalDeletedAt); // Should be ignored
        }

        @Test
        @DisplayName("Should update Product with null optional fields")
        void shouldUpdateProductWithNullOptionalFields() {
            // Given
            Product existingProduct = createCompleteProduct();
            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Product", null, "UPDATED-SKU",
                    BigDecimal.valueOf(200.00), 10, null
            );

            // When
            productMapper.updateProductFromRequest(request, existingProduct);

            // Then
            assertThat(existingProduct.getName()).isEqualTo("Updated Product");
            assertThat(existingProduct.getDescription()).isNull();
            assertThat(existingProduct.getSku()).isEqualTo("UPDATED-SKU");
            assertThat(existingProduct.getPrice()).isEqualTo(BigDecimal.valueOf(200.00));
            assertThat(existingProduct.getMinStockLevel()).isEqualTo(10);
            assertThat(existingProduct.getCategory()).isNull();
        }

        @Test
        @DisplayName("Should update Product with empty strings")
        void shouldUpdateProductWithEmptyStrings() {
            // Given
            Product existingProduct = createCompleteProduct();
            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Product", "", "UPDATED-SKU",
                    BigDecimal.valueOf(200.00), 10, ""
            );

            // When
            productMapper.updateProductFromRequest(request, existingProduct);

            // Then
            assertThat(existingProduct.getName()).isEqualTo("Updated Product");
            assertThat(existingProduct.getDescription()).isEmpty();
            assertThat(existingProduct.getSku()).isEqualTo("UPDATED-SKU");
            assertThat(existingProduct.getCategory()).isEmpty();
        }

        @Test
        @DisplayName("Should preserve ignored fields during update")
        void shouldPreserveIgnoredFieldsDuringUpdate() {
            // Given
            Product existingProduct = createCompleteProduct();
            existingProduct.softDelete(); // This sets deletedAt and active fields

            UUID originalId = existingProduct.getId();
            Boolean originalActive = existingProduct.getActive();
            String originalOriginalSku = existingProduct.getOriginalSku();
            LocalDateTime originalCreatedAt = existingProduct.getCreatedAt();
            LocalDateTime originalUpdatedAt = existingProduct.getUpdatedAt();
            LocalDateTime originalDeletedAt = existingProduct.getDeletedAt();

            UpdateProductRequest request = createCompleteUpdateRequest();

            // When
            productMapper.updateProductFromRequest(request, existingProduct);

            // Then - Ignored fields should remain unchanged
            assertThat(existingProduct.getId()).isEqualTo(originalId);
            assertThat(existingProduct.getActive()).isEqualTo(originalActive);
            assertThat(existingProduct.getOriginalSku()).isEqualTo(originalOriginalSku);
            assertThat(existingProduct.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(existingProduct.getUpdatedAt()).isEqualTo(originalUpdatedAt);
            assertThat(existingProduct.getDeletedAt()).isEqualTo(originalDeletedAt);
        }

        @Test
        @DisplayName("Should update all mappable fields correctly")
        void shouldUpdateAllMappableFieldsCorrectly() {
            // Given
            Product existingProduct = createCompleteProduct();
            UpdateProductRequest request = new UpdateProductRequest(
                    "New Product Name",
                    "New product description",
                    "NEW-SKU-123",
                    BigDecimal.valueOf(299.99),
                    15,
                    "updated-category"
            );

            // When
            productMapper.updateProductFromRequest(request, existingProduct);

            // Then
            assertThat(existingProduct.getName()).isEqualTo("New Product Name");
            assertThat(existingProduct.getDescription()).isEqualTo("New product description");
            assertThat(existingProduct.getSku()).isEqualTo("NEW-SKU-123");
            assertThat(existingProduct.getPrice()).isEqualTo(BigDecimal.valueOf(299.99));
            assertThat(existingProduct.getMinStockLevel()).isEqualTo(15);
            assertThat(existingProduct.getCategory()).isEqualTo("updated-category");
        }
    }

    private Product createCompleteProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("iPhone 15");
        product.setDescription("Latest iPhone model");
        product.setSku("IPHONE15");
        product.setOriginalSku(null);
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setStockQuantity(10);
        product.setMinStockLevel(5);
        product.setCategory("electronics");
        return product;
    }

    private CreateProductRequest createCompleteCreateRequest() {
        return new CreateProductRequest(
                "iPhone 15",
                "Latest iPhone model",
                "IPHONE15",
                BigDecimal.valueOf(999.99),
                10,
                5,
                "electronics"
        );
    }

    private UpdateProductRequest createCompleteUpdateRequest() {
        return new UpdateProductRequest(
                "iPhone 15 Pro",
                "Updated iPhone model",
                "IPHONE15PRO",
                BigDecimal.valueOf(1199.99),
                15,
                "premium-electronics"
        );
    }
}