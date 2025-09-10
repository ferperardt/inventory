package com.inventory.service;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.exception.DuplicateSkuException;
import com.inventory.exception.InvalidStockLevelException;
import com.inventory.exception.ProductHasStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.exception.SupplierNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private SupplierService supplierService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, productMapper, stockMovementService, supplierService);
    }

    @Nested
    @DisplayName("createProduct() Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            UUID supplierId = UUID.randomUUID();
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 10, 5, "electronics",
                    List.of(supplierId)
            );
            Product product = createProduct();
            Supplier supplier = createSupplier();
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);
            given(supplierService.getSupplierEntityById(supplierId)).willReturn(supplier);
            given(productMapper.toEntity(request)).willReturn(product);
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(expectedResponse);

            // When
            ProductResponse result = productService.createProduct(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(productRepository).should().existsBySkuAndActiveTrue(request.sku());
            then(supplierService).should().getSupplierEntityById(supplierId);
            then(productMapper).should().toEntity(request);
            then(productRepository).should().save(product);
            then(stockMovementService).should().createStockMovement(any(CreateStockMovementRequest.class));
            then(productMapper).should().toResponse(product);
        }

        @Test
        @DisplayName("Should throw DuplicateSkuException when SKU already exists")
        void shouldThrowDuplicateSkuExceptionWhenSkuAlreadyExists() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 10, 5, "electronics",
                    List.of(UUID.randomUUID())
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
                    BigDecimal.valueOf(999.99), 5, 10, "electronics",
                    List.of(UUID.randomUUID())
            );

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(InvalidStockLevelException.class)
                    .hasMessageContaining("Stock quantity (5) cannot be below minimum stock level (10)");

            then(productRepository).should().existsBySkuAndActiveTrue(request.sku());
            then(productMapper).should(never()).toEntity(any());
            then(productRepository).should(never()).save(any());
            then(stockMovementService).should(never()).createStockMovement(any());
        }

        @Test
        @DisplayName("Should create product with zero stock and create stock movement")
        void shouldCreateProductWithZeroStockAndCreateStockMovement() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 0, 0, "electronics",
                    List.of(UUID.randomUUID())
            );
            Product product = createProduct(0);
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);
            given(supplierService.getSupplierEntityById(any())).willReturn(createSupplier());
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
            then(stockMovementService).should().createStockMovement(argThat(movementRequest -> {
                return movementRequest.productId().equals(product.getId()) &&
                       movementRequest.movementType() == MovementType.IN &&
                       movementRequest.quantity().equals(0) &&
                       movementRequest.reason() == MovementReason.INITIAL_STOCK;
            }));
            then(productMapper).should().toResponse(product);
        }

        @Test
        @DisplayName("Should create product with null stock and create stock movement with zero quantity")
        void shouldCreateProductWithNullStockAndCreateStockMovementWithZeroQuantity() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), null, 0, "electronics",
                    List.of(UUID.randomUUID())
            );
            Product product = createProduct(0);
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);
            given(supplierService.getSupplierEntityById(any())).willReturn(createSupplier());
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
            then(stockMovementService).should().createStockMovement(argThat(movementRequest -> {
                return movementRequest.productId().equals(product.getId()) &&
                       movementRequest.movementType() == MovementType.IN &&
                       movementRequest.quantity().equals(0) &&  // null becomes 0
                       movementRequest.reason() == MovementReason.INITIAL_STOCK;
            }));
            then(productMapper).should().toResponse(product);
        }

        @Test
        @DisplayName("Should create initial stock movement with correct details")
        void shouldCreateInitialStockMovementWithCorrectDetails() {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 15, 5, "electronics",
                    List.of(UUID.randomUUID())
            );
            Product product = createProduct(15);
            ProductResponse expectedResponse = createProductResponse();

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);
            given(supplierService.getSupplierEntityById(any())).willReturn(createSupplier());
            given(productMapper.toEntity(request)).willReturn(product);
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(expectedResponse);

            // When
            ProductResponse result = productService.createProduct(request);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(stockMovementService).should().createStockMovement(argThat(movementRequest -> {
                return movementRequest.productId().equals(product.getId()) &&
                        movementRequest.movementType() == MovementType.IN &&
                        movementRequest.quantity().equals(15) &&
                        movementRequest.reason() == MovementReason.INITIAL_STOCK &&
                        movementRequest.reference().equals("Initial stock on product creation") &&
                        movementRequest.notes().equals("Initial stock set during product creation");
            }));
        }

        @Test
        @DisplayName("Should throw SupplierNotFoundException when supplier does not exist")
        void shouldThrowSupplierNotFoundExceptionWhenSupplierDoesNotExist() {
            // Given
            UUID supplierId = UUID.randomUUID();
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 10, 5, "electronics",
                    List.of(supplierId)
            );

            given(productRepository.existsBySkuAndActiveTrue(request.sku())).willReturn(false);
            given(supplierService.getSupplierEntityById(supplierId))
                    .willThrow(new SupplierNotFoundException(supplierId));

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(SupplierNotFoundException.class);

            then(productRepository).should().existsBySkuAndActiveTrue(request.sku());
            then(supplierService).should().getSupplierEntityById(supplierId);
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
                    BigDecimal.valueOf(1199.99), 8, "electronics"
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
                    BigDecimal.valueOf(1199.99), 8, "electronics"
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
        @DisplayName("Should throw DuplicateSkuException when updating to existing SKU")
        void shouldThrowDuplicateSkuExceptionWhenUpdatingToExistingSku() {
            // Given
            UUID id = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    "iPhone 15 Pro", "Updated iPhone", "EXISTING_SKU",
                    BigDecimal.valueOf(1199.99), 8, "electronics"
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
            Product product = createProduct(0);

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

        @Test
        @DisplayName("Should throw ProductHasStockException when deleting product with stock > 0")
        void shouldThrowProductHasStockExceptionWhenDeletingProductWithStock() {
            // Given
            UUID id = UUID.randomUUID();
            Product product = createProduct(5); // Product with stock > 0

            given(productRepository.findById(id)).willReturn(Optional.of(product));

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(id))
                    .isInstanceOf(ProductHasStockException.class)
                    .hasMessageContaining("Cannot delete product")
                    .hasMessageContaining("Current stock: 5")
                    .hasMessageContaining("Stock must be zero before deletion");

            then(productRepository).should().findById(id);
            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should throw ProductHasStockException when deleting product with null stock that defaults to > 0")
        void shouldThrowProductHasStockExceptionWhenDeletingProductWithNullStockDefaulting() {
            // Given
            UUID id = UUID.randomUUID();
            Product product = createProduct(null); // null stock
            product.setStockQuantity(10); // but actually has stock

            given(productRepository.findById(id)).willReturn(Optional.of(product));

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(id))
                    .isInstanceOf(ProductHasStockException.class);

            then(productRepository).should().findById(id);
            then(productRepository).should(never()).save(any());
        }
    }

    private Product createProduct() {
        return createProduct(10);
    }

    private Product createProduct(Integer stockQuantity) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("iPhone 15");
        product.setDescription("Latest iPhone");
        product.setSku("IPHONE15");
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setStockQuantity(stockQuantity);
        product.setMinStockLevel(5);
        product.setCategory("electronics");
        return product;
    }

    private Product createSoftDeletedProduct() {
        Product product = createProduct();
        product.softDelete();
        return product;
    }

    private Supplier createSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Test Supplier");
        supplier.setEmail("test@supplier.com");
        supplier.setPhone("123456789");
        return supplier;
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
                LocalDateTime.now(),
                List.of()
        );
    }
}