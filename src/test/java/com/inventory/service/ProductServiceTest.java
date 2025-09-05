package com.inventory.service;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.exception.DuplicateSkuException;
import com.inventory.exception.InvalidStockLevelException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.mapper.ProductMapper;
import com.inventory.repository.ProductRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, productMapper);
    }

    @Nested
    @DisplayName("createProduct() Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 10, 5, "electronics"
            );
            Product product = createProduct();
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);
            given(productMapper.toEntity(request)).willReturn(product);
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(expectedResponse);

            // When
            ProductResponse result = productService.createProduct(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(productRepository).should().existsBySkuAndActiveTrue(request.sku());
            then(productMapper).should().toEntity(request);
            then(productRepository).should().save(product);
            then(productMapper).should().toResponse(product);
        }

        @Test
        @DisplayName("Should throw DuplicateSkuException when SKU already exists")
        void shouldThrowDuplicateSkuExceptionWhenSkuAlreadyExists() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 10, 5, "electronics"
            );

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(DuplicateSkuException.class);

            then(productRepository).should().existsBySkuAndActiveTrue(request.sku());
            then(productMapper).should(never()).toEntity(any());
            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should throw InvalidStockLevelException when stock is below minimum")
        void shouldThrowInvalidStockLevelExceptionWhenStockBelowMinimum() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 5, 10, "electronics"
            );

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(InvalidStockLevelException.class)
                    .hasMessageContaining("Stock quantity (5) cannot be below minimum stock level (10)");

            then(productRepository).should().existsBySkuAndActiveTrue(request.sku());
            then(productMapper).should(never()).toEntity(any());
            then(productRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllProducts() Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated active products")
        void shouldReturnPaginatedActiveProducts() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Product product = createProduct();
            ProductResponse productResponse = createProductResponse();
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.findByActiveTrue(pageable)).willReturn(productPage);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            // When
            Page<ProductResponse> result = productService.getAllProducts(pageable);

            // Then
            assertThat(result.getContent()).containsExactly(productResponse);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            then(productRepository).should().findByActiveTrue(pageable);
        }
    }

    @Nested
    @DisplayName("getProductById() Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when ID exists and is active")
        void shouldReturnProductWhenIdExistsAndIsActive() {
            // Given
            UUID id = UUID.randomUUID();
            Product product = createProduct();
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.findById(id)).willReturn(Optional.of(product));
            given(productMapper.toResponse(product)).willReturn(expectedResponse);

            // When
            ProductResponse result = productService.getProductById(id);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(productRepository).should().findById(id);
            then(productMapper).should().toResponse(product);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when ID does not exist")
        void shouldThrowProductNotFoundExceptionWhenIdDoesNotExist() {
            // Given
            UUID id = UUID.randomUUID();
            given(productRepository.findById(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.getProductById(id))
                    .isInstanceOf(ProductNotFoundException.class);

            then(productRepository).should().findById(id);
            then(productMapper).should(never()).toResponse(any());
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product is soft deleted")
        void shouldThrowProductNotFoundExceptionWhenProductSoftDeleted() {
            // Given
            UUID id = UUID.randomUUID();
            Product product = createSoftDeletedProduct();

            given(productRepository.findById(id)).willReturn(Optional.of(product));

            // When & Then
            assertThatThrownBy(() -> productService.getProductById(id))
                    .isInstanceOf(ProductNotFoundException.class);

            then(productRepository).should().findById(id);
            then(productMapper).should(never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("getProductBySku() Tests")
    class GetProductBySkuTests {

        @Test
        @DisplayName("Should return product when SKU exists and is active")
        void shouldReturnProductWhenSkuExistsAndIsActive() {
            // Given
            String sku = "IPHONE15";
            Product product = createProduct();
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.findBySkuAndActiveTrue(sku)).willReturn(Optional.of(product));
            given(productMapper.toResponse(product)).willReturn(expectedResponse);

            // When
            ProductResponse result = productService.getProductBySku(sku);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(productRepository).should().findBySkuAndActiveTrue(sku);
            then(productMapper).should().toResponse(product);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when SKU does not exist")
        void shouldThrowProductNotFoundExceptionWhenSkuDoesNotExist() {
            // Given
            String sku = "NONEXISTENT";
            given(productRepository.findBySkuAndActiveTrue(sku)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.getProductBySku(sku))
                    .isInstanceOf(ProductNotFoundException.class);

            then(productRepository).should().findBySkuAndActiveTrue(sku);
            then(productMapper).should(never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("searchProducts() Tests")
    class SearchProductsTests {

        @Test
        @DisplayName("Should search products with all filters")
        void shouldSearchProductsWithAllFilters() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Product product = createProduct();
            ProductResponse productResponse = createProductResponse();
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), eq(pageable))).willReturn(productPage);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            // When
            Page<ProductResponse> result = productService.searchProducts(
                    "iPhone", "electronics", "IPHONE15", "Latest",
                    BigDecimal.valueOf(500), BigDecimal.valueOf(1500),
                    5, 20, true, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(productResponse);
            then(productRepository).should().findAll(ArgumentMatchers.<Specification<Product>>any(), eq(pageable));
        }

        @Test
        @DisplayName("Should search products with only active filter when no other filters provided")
        void shouldSearchProductsWithOnlyActiveFilterWhenNoOtherFiltersProvided() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Product product = createProduct();
            ProductResponse productResponse = createProductResponse();
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            given(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), eq(pageable))).willReturn(productPage);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            // When
            Page<ProductResponse> result = productService.searchProducts(
                    null, null, null, null,
                    null, null, null, null, null, pageable
            );

            // Then
            assertThat(result.getContent()).containsExactly(productResponse);
            then(productRepository).should().findAll(ArgumentMatchers.<Specification<Product>>any(), eq(pageable));
        }
    }

    @Nested
    @DisplayName("updateProduct() Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            // Given
            UUID id = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    "iPhone 15 Pro", "Updated iPhone", "IPHONE15PRO",
                    BigDecimal.valueOf(1199.99), 15, 8, "electronics"
            );
            Product existingProduct = createProduct();
            existingProduct.setSku("IPHONE15");
            Product updatedProduct = createProduct();
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);
            given(productRepository.save(existingProduct)).willReturn(updatedProduct);
            given(productMapper.toResponse(updatedProduct)).willReturn(expectedResponse);

            // When
            ProductResponse result = productService.updateProduct(id, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(productRepository).should().findById(id);
            then(productMapper).should().updateProductFromRequest(request, existingProduct);
            then(productRepository).should().save(existingProduct);
        }

        @Test
        @DisplayName("Should update product with same SKU successfully")
        void shouldUpdateProductWithSameSkuSuccessfully() {
            // Given
            UUID id = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    "iPhone 15 Pro", "Updated iPhone", "IPHONE15",  // Same SKU
                    BigDecimal.valueOf(1199.99), 15, 8, "electronics"
            );
            Product existingProduct = createProduct();
            existingProduct.setSku("IPHONE15");
            Product updatedProduct = createProduct();
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
            given(productRepository.save(existingProduct)).willReturn(updatedProduct);
            given(productMapper.toResponse(updatedProduct)).willReturn(expectedResponse);

            // When
            ProductResponse result = productService.updateProduct(id, request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(productRepository).should().findById(id);
            then(productRepository).should(never()).existsBySkuAndActiveTrue(any());
            then(productMapper).should().updateProductFromRequest(request, existingProduct);
            then(productRepository).should().save(existingProduct);
        }

        @Test
        @DisplayName("Should throw InvalidStockLevelException when updating with invalid stock")
        void shouldThrowInvalidStockLevelExceptionWhenUpdatingWithInvalidStock() {
            // Given
            UUID id = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    "iPhone 15 Pro", "Updated iPhone", "IPHONE15PRO",
                    BigDecimal.valueOf(1199.99), 5, 15, "electronics"  // Stock < minStock
            );
            Product existingProduct = createProduct();

            given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(id, request))
                    .isInstanceOf(InvalidStockLevelException.class);

            then(productRepository).should().findById(id);
            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateSkuException when updating to existing SKU")
        void shouldThrowDuplicateSkuExceptionWhenUpdatingToExistingSku() {
            // Given
            UUID id = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    "iPhone 15 Pro", "Updated iPhone", "EXISTING_SKU",
                    BigDecimal.valueOf(1199.99), 15, 8, "electronics"
            );
            Product existingProduct = createProduct();
            existingProduct.setSku("IPHONE15");

            given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(id, request))
                    .isInstanceOf(DuplicateSkuException.class);

            then(productRepository).should().findById(id);
            then(productRepository).should().existsBySkuAndActiveTrue(request.sku());
            then(productRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteProduct() Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should soft delete product successfully")
        void shouldSoftDeleteProductSuccessfully() {
            // Given
            UUID id = UUID.randomUUID();
            Product product = createProduct();

            given(productRepository.findById(id)).willReturn(Optional.of(product));

            // When
            productService.deleteProduct(id);

            // Then
            then(productRepository).should().findById(id);
            then(productRepository).should().save(product);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
        void shouldThrowProductNotFoundExceptionWhenDeletingNonExistentProduct() {
            // Given
            UUID id = UUID.randomUUID();
            given(productRepository.findById(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(id))
                    .isInstanceOf(ProductNotFoundException.class);

            then(productRepository).should().findById(id);
            then(productRepository).should(never()).save(any());
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

    private Product createSoftDeletedProduct() {
        Product product = createProduct();
        product.softDelete();
        return product;
    }


    private ProductResponse createProductResponse() {
        return new ProductResponse(
                UUID.randomUUID(),
                "iPhone 15",
                "Latest iPhone",
                "IPHONE15",
                BigDecimal.valueOf(999.99),
                10,
                5,
                "electronics",
                true,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}