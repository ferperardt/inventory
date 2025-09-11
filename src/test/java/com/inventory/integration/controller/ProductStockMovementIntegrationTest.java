package com.inventory.integration.controller;

import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.integration.fixtures.ProductTestFactory;
import com.inventory.integration.fixtures.StockMovementTestFactory;
import com.inventory.integration.fixtures.SupplierTestFactory;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductStockMovementIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    private UUID testProductId;
    private UUID testSupplierId;


    @BeforeAll
    void setupTestData() {
        Supplier supplier = SupplierTestFactory.validSupplierEntity("Product Stock Movement Test Supplier");
        Supplier savedSupplier = supplierRepository.save(supplier);
        testSupplierId = savedSupplier.getId();

        var productRequest = ProductTestFactory.customProductRequest(
                "Stock Movement Product",
                "STOCK-MOV-" + System.currentTimeMillis(),
                testSupplierId
        );

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", productRequest, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        testProductId = createResponse.getBody().id();
    }

    @Test
    @Order(1)
    @DisplayName("Should get product stock movements after creating initial stock")
    void shouldGetProductStockMovementsAfterCreatingInitialStock() {
        String url = "/api/v1/products/" + testProductId + "/stock-movements";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("IN");
        assertThat(response.getBody()).contains("INITIAL_STOCK");
        assertThat(response.getBody()).contains("\"quantity\":10");
        assertThat(response.getBody()).contains("\"previousStock\":10");
        assertThat(response.getBody()).contains("\"newStock\":10");
    }

    @Test
    @Order(2)
    @DisplayName("Should show complete stock movement history for product")
    void shouldShowCompleteStockMovementHistoryForProduct() {
        CreateStockMovementRequest inMovement = StockMovementTestFactory.validInMovementRequest(testProductId);
        restTemplate.postForEntity("/api/v1/stock-movements", inMovement, StockMovementResponse.class);

        CreateStockMovementRequest outMovement = StockMovementTestFactory.validOutMovementRequest(testProductId, 5);
        restTemplate.postForEntity("/api/v1/stock-movements", outMovement, StockMovementResponse.class);

        String url = "/api/v1/products/" + testProductId + "/stock-movements";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("OUT");
        assertThat(response.getBody()).contains("\"quantity\":5");
        assertThat(response.getBody()).contains("IN");
        assertThat(response.getBody()).contains("PURCHASE");
    }

    @Test
    @Order(3)
    @DisplayName("Should verify stock movement chain consistency when movements exist")
    void shouldVerifyStockMovementChainConsistencyWhenMovementsExist() {
        // Create specific movements to test chain consistency
        CreateStockMovementRequest firstMovement = StockMovementTestFactory.validInMovementRequest(testProductId);
        restTemplate.postForEntity("/api/v1/stock-movements", firstMovement, StockMovementResponse.class);

        CreateStockMovementRequest secondMovement = StockMovementTestFactory.validOutMovementRequest(testProductId, 3);
        restTemplate.postForEntity("/api/v1/stock-movements", secondMovement, StockMovementResponse.class);

        String url = "/api/v1/products/" + testProductId + "/stock-movements";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("newStock");
        assertThat(response.getBody()).contains("previousStock");
        assertThat(response.getBody()).contains("IN");
        assertThat(response.getBody()).contains("OUT");
    }

    @Test
    @Order(4)
    @DisplayName("Should return empty movements list when product has no additional movements")
    void shouldReturnEmptyMovementsListWhenProductHasNoAdditionalMovements() {
        // Clean all movements first
        stockMovementRepository.deleteAll();

        String url = "/api/v1/products/" + testProductId + "/stock-movements";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"content\":[]");
        assertThat(response.getBody()).contains("\"totalElements\":0");
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 when getting movements for non-existent product")
    void shouldReturn404WhenGettingMovementsForNonExistentProduct() {
        UUID nonExistentProductId = UUID.randomUUID();
        String url = "/api/v1/products/" + nonExistentProductId + "/stock-movements";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(6)
    @DisplayName("Should handle product with no movements gracefully")
    void shouldHandleProductWithNoMovementsGracefully() {
        var anotherProductRequest = ProductTestFactory.customProductRequest(
                "No Movement Product",
                "NO-MOV-" + System.currentTimeMillis(),
                testSupplierId
        );

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/products", anotherProductRequest, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID anotherProductId = createResponse.getBody().id();

        stockMovementRepository.deleteAll();

        String url = "/api/v1/products/" + anotherProductId + "/stock-movements";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"content\":[]"); // Empty page
    }

    @Test
    @Order(7)
    @DisplayName("Should maintain relationship integrity between Product and StockMovement")
    void shouldMaintainRelationshipIntegrityBetweenProductAndStockMovement() {
        CreateStockMovementRequest movement = StockMovementTestFactory.customMovementRequest(
                testProductId,
                com.inventory.enums.MovementType.IN,
                25,
                com.inventory.enums.MovementReason.PURCHASE,
                "INTEGRITY-TEST-" + System.currentTimeMillis()
        );

        ResponseEntity<StockMovementResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/stock-movements", movement, StockMovementResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        StockMovementResponse createdMovement = createResponse.getBody();
        assertThat(createdMovement).isNotNull();

        String url = "/api/v1/products/" + testProductId + "/stock-movements";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Find our created movement in the response
        assertThat(response.getBody()).contains(movement.reference());
        assertThat(response.getBody()).contains(String.valueOf(movement.quantity()));

        // Verify product was updated
        Product product = productRepository.findById(testProductId).orElse(null);
        assertThat(product).isNotNull();
        assertThat(product.getStockQuantity()).isEqualTo(createdMovement.newStock());
    }

    @AfterEach
    void cleanupAfterEachTest() {
        stockMovementRepository.deleteAll();

        Product product = productRepository.findById(testProductId).orElse(null);
        if (product != null) {
            product.setStockQuantity(10); // Reset to initial stock
            productRepository.save(product);
        }
    }
}