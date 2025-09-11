package com.inventory.integration.transaction;

import com.inventory.dto.request.CreateProductRequest;
import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import com.inventory.entity.Supplier;
import com.inventory.integration.fixtures.ProductTestFactory;
import com.inventory.integration.fixtures.StockMovementTestFactory;
import com.inventory.integration.fixtures.SupplierTestFactory;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.ProductService;
import com.inventory.service.StockMovementService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionalIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    private UUID testSupplierId;

    @BeforeAll
    void setupTestData() {
        Supplier supplier = SupplierTestFactory.validSupplierEntity("Transactional Test Supplier");
        Supplier savedSupplier = supplierRepository.save(supplier);
        testSupplierId = savedSupplier.getId();
    }

    @Test
    @Order(1)
    @DisplayName("Should create product with initial stock movement in single transaction")
    void shouldCreateProductWithInitialStockMovementInSingleTransaction() {
        long productsCountBefore = productRepository.count();
        long movementsCountBefore = stockMovementRepository.count();

        CreateProductRequest request = ProductTestFactory.customProductRequest(
                "Transactional Test Product",
                "TRANS-TEST-" + System.currentTimeMillis(),
                testSupplierId
        );

        ResponseEntity<ProductResponse> response = restTemplate.postForEntity(
                "/api/v1/products", request, ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        long productsCountAfter = productRepository.count();
        long movementsCountAfter = stockMovementRepository.count();

        assertThat(productsCountAfter).isEqualTo(productsCountBefore + 1);
        assertThat(movementsCountAfter).isEqualTo(movementsCountBefore + 1);

        Product savedProduct = productRepository.findBySkuAndActiveTrue(request.sku()).orElse(null);
        assertThat(savedProduct).isNotNull();

        assertThat(savedProduct.getStockQuantity()).isEqualTo(10);

        List<StockMovement> movements = stockMovementRepository.findAll()
                .stream()
                .filter(m -> m.getProduct() != null && m.getProduct().getId().equals(savedProduct.getId()))
                .toList();
        assertThat(movements).hasSize(1);
        assertThat(movements.get(0).getQuantity()).isEqualTo(10);
        assertThat(movements.get(0).getReason().name()).isEqualTo("INITIAL_STOCK");
    }

    @Test
    @Order(2)
    @DisplayName("Should handle product creation failure gracefully")
    void shouldHandleProductCreationFailureGracefully() {
        long productsCountBefore = productRepository.count();

        CreateProductRequest requestWithNonExistentSupplier = ProductTestFactory.productWithNonExistentSupplier();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/products", requestWithNonExistentSupplier, String.class);

        // Should fail with either 404 or 500 depending on exception handling
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        long productsCountAfter = productRepository.count();
        assertThat(productsCountAfter).isEqualTo(productsCountBefore);

        boolean productExists = productRepository.existsBySkuAndActiveTrue(requestWithNonExistentSupplier.sku());
        assertThat(productExists).isFalse();
    }

    @Test
    @Order(3)
    @DisplayName("Should maintain stock consistency when stock movement succeeds")
    void shouldMaintainStockConsistencyWhenStockMovementSucceeds() {
        CreateProductRequest productRequest = ProductTestFactory.validProductRequest(testSupplierId);
        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", productRequest, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID productId = createResponse.getBody().id();

        Product productBefore = productRepository.findById(productId).orElse(null);
        assertThat(productBefore).isNotNull();
        Integer stockBefore = productBefore.getStockQuantity();

        CreateStockMovementRequest movementRequest = StockMovementTestFactory.validInMovementRequest(productId);

        ResponseEntity<Object> movementResponse = restTemplate.postForEntity(
                "/api/v1/stock-movements", movementRequest, Object.class);

        assertThat(movementResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Product productAfter = productRepository.findById(productId).orElse(null);
        assertThat(productAfter).isNotNull();
        assertThat(productAfter.getStockQuantity()).isEqualTo(stockBefore + movementRequest.quantity());

        List<StockMovement> movements = stockMovementRepository.findByProductIdAndActiveTrueOrderByCreatedAtDesc(
                productId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        assertThat(movements).hasSizeGreaterThanOrEqualTo(1);

        StockMovement latestMovement = movements.get(0);
        assertThat(latestMovement.getNewStock()).isEqualTo(productAfter.getStockQuantity());
    }

    @Test
    @Order(4)
    @DisplayName("Should handle insufficient stock gracefully without corrupting data")
    void shouldHandleInsufficientStockGracefullyWithoutCorruptingData() {
        CreateProductRequest productRequest = ProductTestFactory.customProductRequest(
                "Low Stock Product",
                "LOW-STOCK-" + System.currentTimeMillis(),
                testSupplierId
        );
        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", productRequest, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID productId = createResponse.getBody().id();

        Product productBefore = productRepository.findById(productId).orElse(null);
        assertThat(productBefore).isNotNull();
        Integer stockBefore = productBefore.getStockQuantity();

        long movementsCountBefore = stockMovementRepository.count();

        CreateStockMovementRequest insufficientMovement = StockMovementTestFactory
                .insufficientStockMovementRequest(productId, stockBefore + 100);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/stock-movements", insufficientMovement, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        Product productAfter = productRepository.findById(productId).orElse(null);
        assertThat(productAfter).isNotNull();
        assertThat(productAfter.getStockQuantity()).isEqualTo(stockBefore);

        long movementsCountAfter = stockMovementRepository.count();
        assertThat(movementsCountAfter).isEqualTo(movementsCountBefore);
    }

    @Test
    @Order(5)
    @DisplayName("Should handle concurrent stock movements consistently")
    void shouldHandleConcurrentStockMovementsConsistently() {
        CreateProductRequest productRequest = ProductTestFactory.customProductRequest(
                "Concurrent Test Product",
                "CONCURRENT-" + System.currentTimeMillis(),
                testSupplierId
        );
        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", productRequest, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID productId = createResponse.getBody().id();

        CreateStockMovementRequest firstMovement = StockMovementTestFactory
                .customMovementRequest(productId, com.inventory.enums.MovementType.IN, 50,
                        com.inventory.enums.MovementReason.PURCHASE, "CONCURRENT-1");

        CreateStockMovementRequest secondMovement = StockMovementTestFactory
                .customMovementRequest(productId, com.inventory.enums.MovementType.OUT, 20,
                        com.inventory.enums.MovementReason.SALE, "CONCURRENT-2");

        ResponseEntity<Object> response1 = restTemplate.postForEntity(
                "/api/v1/stock-movements", firstMovement, Object.class);
        ResponseEntity<Object> response2 = restTemplate.postForEntity(
                "/api/v1/stock-movements", secondMovement, Object.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Product finalProduct = productRepository.findById(productId).orElse(null);
        assertThat(finalProduct).isNotNull();

        List<StockMovement> allMovements = stockMovementRepository
                .findByProductIdAndActiveTrueOrderByCreatedAtDesc(productId,
                        org.springframework.data.domain.Pageable.unpaged()).getContent();

        assertThat(allMovements).hasSizeGreaterThanOrEqualTo(3); // Initial + 2 new movements

        StockMovement latestMovement = allMovements.get(0);
        assertThat(latestMovement.getNewStock()).isEqualTo(finalProduct.getStockQuantity());

        // Verify stock calculation chain integrity
        for (int i = 0; i < allMovements.size() - 1; i++) {
            StockMovement current = allMovements.get(i);
            StockMovement next = allMovements.get(i + 1);
            assertThat(current.getPreviousStock()).isEqualTo(next.getNewStock());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should rollback product deletion when product has stock")
    @Transactional
    void shouldRollbackProductDeletionWhenProductHasStock() {
        CreateProductRequest productRequest = ProductTestFactory.customProductRequest(
                "Stock Product for Deletion",
                "DELETE-WITH-STOCK-" + System.currentTimeMillis(),
                testSupplierId
        );
        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", productRequest, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID productId = createResponse.getBody().id();

        Product productBefore = productRepository.findById(productId).orElse(null);
        assertThat(productBefore).isNotNull();
        assertThat(productBefore.getStockQuantity()).isGreaterThan(0);

        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/api/v1/products/" + productId,
                org.springframework.http.HttpMethod.DELETE,
                null, String.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        Product productAfter = productRepository.findById(productId).orElse(null);
        assertThat(productAfter).isNotNull();
        assertThat(productAfter.getActive()).isTrue();
        assertThat(productAfter.getStockQuantity()).isGreaterThan(0);
    }

    @AfterEach
    void cleanupAfterEachTest() {
        stockMovementRepository.deleteAll();
        productRepository.deleteAll();
    }
}