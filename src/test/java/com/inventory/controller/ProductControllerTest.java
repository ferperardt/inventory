package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.UpdateProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.exception.DuplicateSkuException;
import com.inventory.exception.GlobalExceptionHandler;
import com.inventory.exception.InvalidStockLevelException;
import com.inventory.exception.ProductHasStockException;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.service.ProductService;
import com.inventory.service.StockMovementService;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.enums.MovementType;
import com.inventory.enums.MovementReason;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private StockMovementService stockMovementService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productService, stockMovementService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() throws Exception {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 10, 5, "electronics",
                    List.of(UUID.randomUUID())
            );
            ProductResponse response = createProductResponse();

            given(productService.createProduct(any(CreateProductRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("iPhone 15"))
                    .andExpect(jsonPath("$.sku").value("IPHONE15"))
                    .andExpect(jsonPath("$.price").value(999.99));

            then(productService).should().createProduct(any(CreateProductRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given
            CreateProductRequest invalidRequest = new CreateProductRequest(
                    "", "Description", "INVALID_SKU_WITH_LOWERCASE", // Invalid name and SKU
                    BigDecimal.valueOf(-1), -5, -1, "electronics", // Invalid price and quantities
                    List.of(UUID.randomUUID())
            );

            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 when SKU already exists")
        void shouldReturn409WhenSkuAlreadyExists() throws Exception {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 10, 5, "electronics",
                    List.of(UUID.randomUUID())
            );

            given(productService.createProduct(any(CreateProductRequest.class)))
                    .willThrow(new DuplicateSkuException("IPHONE15"));

            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 422 when stock level is invalid")
        void shouldReturn422WhenStockLevelIsInvalid() throws Exception {
            // Given
            CreateProductRequest request = new CreateProductRequest(
                    "iPhone 15", "Latest iPhone", "IPHONE15",
                    BigDecimal.valueOf(999.99), 5, 10, "electronics", // stock < minStock
                    List.of(UUID.randomUUID())
            );

            given(productService.createProduct(any(CreateProductRequest.class)))
                    .willThrow(new InvalidStockLevelException("Stock quantity (5) cannot be below minimum stock level (10)"));

            // When & Then
            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products")
        void shouldReturnPaginatedProducts() throws Exception {
            // Given
            ProductResponse product = createProductResponse();
            Page<ProductResponse> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

            given(productService.getAllProducts(any(Pageable.class))).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));

            then(productService).should().getAllProducts(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when no products")
        void shouldReturnEmptyPageWhenNoProducts() throws Exception {
            // Given
            Page<ProductResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            given(productService.getAllProducts(any(Pageable.class))).willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            then(productService).should().getAllProducts(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product by ID")
        void shouldReturnProductById() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            ProductResponse response = createProductResponse();

            given(productService.getProductById(productId)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}", productId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("iPhone 15"))
                    .andExpect(jsonPath("$.sku").value("IPHONE15"));

            then(productService).should().getProductById(productId);
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();

            given(productService.getProductById(productId))
                    .willThrow(new ProductNotFoundException(productId));

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}", productId))
                    .andExpect(status().isNotFound());

            then(productService).should().getProductById(productId);
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID")
        void shouldReturn400ForInvalidUuid() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}", "invalid-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                    .andExpect(jsonPath("$.parameter").value("id"))
                    .andExpect(jsonPath("$.invalidValue").value("invalid-uuid"))
                    .andExpect(jsonPath("$.expectedType").value("UUID"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/sku/{sku}")
    class GetProductBySkuTests {

        @Test
        @DisplayName("Should return product by SKU")
        void shouldReturnProductBySku() throws Exception {
            // Given
            String sku = "IPHONE15";
            ProductResponse response = createProductResponse();

            given(productService.getProductBySku(sku)).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/v1/products/sku/{sku}", sku))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("iPhone 15"))
                    .andExpect(jsonPath("$.sku").value("IPHONE15"));

            then(productService).should().getProductBySku(sku);
        }

        @Test
        @DisplayName("Should return 404 when SKU not found")
        void shouldReturn404WhenSkuNotFound() throws Exception {
            // Given
            String sku = "NONEXISTENT";

            given(productService.getProductBySku(sku))
                    .willThrow(new ProductNotFoundException(sku));

            // When & Then
            mockMvc.perform(get("/api/v1/products/sku/{sku}", sku))
                    .andExpect(status().isNotFound());

            then(productService).should().getProductBySku(sku);
        }

    }

    @Nested
    @DisplayName("PUT /api/v1/products/{id}")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    "iPhone 15 Pro", "Updated iPhone", "IPHONE15PRO",
                    BigDecimal.valueOf(1199.99), 15, "electronics"
            );
            ProductResponse response = createProductResponse();

            given(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .willReturn(response);

            // When & Then
            mockMvc.perform(put("/api/v1/products/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("iPhone 15"));

            then(productService).should().updateProduct(eq(productId), any(UpdateProductRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            UpdateProductRequest request = new UpdateProductRequest(
                    "iPhone 15 Pro", "Updated iPhone", "IPHONE15PRO",
                    BigDecimal.valueOf(1199.99), 15, "electronics"
            );

            given(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .willThrow(new ProductNotFoundException(productId));

            // When & Then
            mockMvc.perform(put("/api/v1/products/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            UpdateProductRequest invalidRequest = new UpdateProductRequest(
                    "", "Description", "invalid-sku",
                    BigDecimal.valueOf(-1), -5, "electronics"
            );

            // When & Then
            mockMvc.perform(put("/api/v1/products/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID")
        void shouldReturn400ForInvalidUuid() throws Exception {
            // Given
            UpdateProductRequest request = new UpdateProductRequest(
                    "Valid Product", "Description", "VALID-SKU",
                    BigDecimal.valueOf(100.00), 10, "electronics"
            );

            // When & Then
            mockMvc.perform(put("/api/v1/products/{id}", "invalid-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                    .andExpect(jsonPath("$.parameter").value("id"))
                    .andExpect(jsonPath("$.invalidValue").value("invalid-uuid"))
                    .andExpect(jsonPath("$.expectedType").value("UUID"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();

            // When & Then
            mockMvc.perform(delete("/api/v1/products/{id}", productId))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            then(productService).should().deleteProduct(productId);
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();

            willThrow(new ProductNotFoundException(productId))
                    .given(productService).deleteProduct(productId);

            // When & Then
            mockMvc.perform(delete("/api/v1/products/{id}", productId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID")
        void shouldReturn400ForInvalidUuid() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/v1/products/{id}", "invalid-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                    .andExpect(jsonPath("$.parameter").value("id"))
                    .andExpect(jsonPath("$.invalidValue").value("invalid-uuid"))
                    .andExpect(jsonPath("$.expectedType").value("UUID"));
        }

        @Test
        @DisplayName("Should return 422 when product has stock")
        void shouldReturn422WhenProductHasStock() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();

            willThrow(new ProductHasStockException("IPHONE15", 5))
                    .given(productService).deleteProduct(productId);

            // When & Then
            mockMvc.perform(delete("/api/v1/products/{id}", productId))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.error").value("Product Has Stock"))
                    .andExpect(jsonPath("$.message").value("Cannot delete product IPHONE15. Current stock: 5. Stock must be zero before deletion."));

            then(productService).should().deleteProduct(productId);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/search")
    class SearchProductsTests {

        @Test
        @DisplayName("Should search products with all filters")
        void shouldSearchProductsWithAllFilters() throws Exception {
            // Given
            ProductResponse product = createProductResponse();
            Page<ProductResponse> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

            given(productService.searchProducts(
                    eq("iPhone"), eq("electronics"), eq("IPHONE15"), eq("Latest"),
                    eq(BigDecimal.valueOf(500)), eq(BigDecimal.valueOf(1500)),
                    eq(5), eq(20), eq(true), any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/products/search")
                            .param("name", "iPhone")
                            .param("category", "electronics")
                            .param("sku", "IPHONE15")
                            .param("description", "Latest")
                            .param("minPrice", "500")
                            .param("maxPrice", "1500")
                            .param("minStock", "5")
                            .param("maxStock", "20")
                            .param("lowStock", "true"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Should search products without filters")
        void shouldSearchProductsWithoutFilters() throws Exception {
            // Given
            ProductResponse product = createProductResponse();
            Page<ProductResponse> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

            given(productService.searchProducts(
                    eq(null), eq(null), eq(null), eq(null),
                    eq(null), eq(null), eq(null), eq(null), eq(null),
                    any(Pageable.class)
            )).willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/products/search"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15"));
        }

    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}/stock-movements")
    class GetProductStockMovementsTests {

        @Test
        @DisplayName("Should return paginated stock movements for product")
        void shouldReturnPaginatedStockMovements() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            StockMovementResponse movement1 = createStockMovementResponse(productId, MovementType.OUT, 10);
            StockMovementResponse movement2 = createStockMovementResponse(productId, MovementType.IN, 50);
            
            Page<StockMovementResponse> page = new PageImpl<>(
                List.of(movement1, movement2), 
                PageRequest.of(0, 20), 
                2
            );

            given(stockMovementService.getMovementsByProductId(eq(productId), any(Pageable.class)))
                    .willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}/stock-movements", productId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].productId").value(productId.toString()))
                    .andExpect(jsonPath("$.content[0].movementType").value("OUT"))
                    .andExpect(jsonPath("$.content[0].quantity").value(10))
                    .andExpect(jsonPath("$.content[1].movementType").value("IN"))
                    .andExpect(jsonPath("$.content[1].quantity").value(50))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(20));

            then(stockMovementService).should().getMovementsByProductId(eq(productId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when product has no movements")
        void shouldReturnEmptyPageWhenNoMovements() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            Page<StockMovementResponse> emptyPage = new PageImpl<>(
                List.of(), 
                PageRequest.of(0, 20), 
                0
            );

            given(stockMovementService.getMovementsByProductId(eq(productId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}/stock-movements", productId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0))
                    .andExpect(jsonPath("$.empty").value(true));

            then(stockMovementService).should().getMovementsByProductId(eq(productId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle pagination parameters")
        void shouldHandlePaginationParameters() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();
            StockMovementResponse movement = createStockMovementResponse(productId, MovementType.IN, 25);
            
            Page<StockMovementResponse> page = new PageImpl<>(
                List.of(movement), 
                PageRequest.of(1, 5), 
                10
            );

            given(stockMovementService.getMovementsByProductId(eq(productId), any(Pageable.class)))
                    .willReturn(page);

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}/stock-movements", productId)
                            .param("page", "1")
                            .param("size", "5")
                            .param("sort", "createdAt,desc"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.totalElements").value(10))
                    .andExpect(jsonPath("$.totalPages").value(2));

            then(stockMovementService).should().getMovementsByProductId(eq(productId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Given
            UUID productId = UUID.randomUUID();

            willThrow(new ProductNotFoundException(productId))
                    .given(stockMovementService).getMovementsByProductId(eq(productId), any(Pageable.class));

            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}/stock-movements", productId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Product Not Found"))
                    .andExpect(jsonPath("$.message").value("Product not found with id: " + productId));

            then(stockMovementService).should().getMovementsByProductId(eq(productId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID")
        void shouldReturn400ForInvalidUuid() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/products/{id}/stock-movements", "invalid-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Invalid Parameter"))
                    .andExpect(jsonPath("$.parameter").value("id"))
                    .andExpect(jsonPath("$.invalidValue").value("invalid-uuid"))
                    .andExpect(jsonPath("$.expectedType").value("UUID"));
        }
    }

    private StockMovementResponse createStockMovementResponse(UUID productId, MovementType type, Integer quantity) {
        return new StockMovementResponse(
                UUID.randomUUID(),
                productId,
                "TEST-SKU",
                "Test Product",
                type,
                quantity,
                type == MovementType.IN ? 20 : 30,
                type == MovementType.IN ? 20 + quantity : 30 - quantity,
                type == MovementType.IN ? MovementReason.PURCHASE : MovementReason.SALE,
                "REF-001",
                "Test notes",
                "system",
                LocalDateTime.now()
        );
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