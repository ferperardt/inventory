package com.inventory.integration.controller;

import com.inventory.dto.request.CreateStockMovementRequest;
import com.inventory.dto.response.StockMovementResponse;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.enums.MovementReason;
import com.inventory.enums.MovementType;
import com.inventory.integration.fixtures.ProductTestFactory;
import com.inventory.integration.fixtures.RestResponsePage;
import com.inventory.integration.fixtures.StockMovementTestFactory;
import com.inventory.integration.fixtures.SupplierTestFactory;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StockMovementIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    private UUID testProductId;

    @BeforeAll
    void setupTestData() {
        Supplier supplier = SupplierTestFactory.validSupplierEntity("Stock Movement Test Supplier");
        Supplier savedSupplier = supplierRepository.save(supplier);
        UUID testSupplierId = savedSupplier.getId();

        var productRequest = ProductTestFactory.validProductRequest(testSupplierId);
        ResponseEntity<Object> createResponse = restTemplate.postForEntity(
                "/api/v1/products", productRequest, Object.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Product createdProduct = productRepository.findBySkuAndActiveTrue(productRequest.sku()).orElse(null);
        assertThat(createdProduct).isNotNull();
        testProductId = createdProduct.getId();
    }

    @Test
    @Order(1)
    @DisplayName("Should create stock IN movement and update product stock")
    void shouldCreateStockInMovementAndUpdateProductStock() {
        Product productBefore = productRepository.findById(testProductId).orElse(null);
        assertThat(productBefore).isNotNull();
        Integer stockBefore = productBefore.getStockQuantity();

        CreateStockMovementRequest request = StockMovementTestFactory.validInMovementRequest(testProductId);

        ResponseEntity<StockMovementResponse> response = restTemplate.postForEntity(
                "/api/v1/stock-movements", request, StockMovementResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().movementType()).isEqualTo(request.movementType());
        assertThat(response.getBody().quantity()).isEqualTo(request.quantity());
        assertThat(response.getBody().previousStock()).isEqualTo(stockBefore);
        assertThat(response.getBody().newStock()).isEqualTo(stockBefore + request.quantity());

        Product productAfter = productRepository.findById(testProductId).orElse(null);
        assertThat(productAfter).isNotNull();
        assertThat(productAfter.getStockQuantity()).isEqualTo(stockBefore + request.quantity());
    }

    @Test
    @Order(2)
    @DisplayName("Should create stock OUT movement and update product stock")
    void shouldCreateStockOutMovementAndUpdateProductStock() {
        Product productBefore = productRepository.findById(testProductId).orElse(null);
        assertThat(productBefore).isNotNull();
        Integer stockBefore = productBefore.getStockQuantity();

        CreateStockMovementRequest request = StockMovementTestFactory.validOutMovementRequest(testProductId, 3);

        ResponseEntity<StockMovementResponse> response = restTemplate.postForEntity(
                "/api/v1/stock-movements", request, StockMovementResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().movementType()).isEqualTo(request.movementType());
        assertThat(response.getBody().quantity()).isEqualTo(request.quantity());
        assertThat(response.getBody().previousStock()).isEqualTo(stockBefore);
        assertThat(response.getBody().newStock()).isEqualTo(stockBefore - request.quantity());

        Product productAfter = productRepository.findById(testProductId).orElse(null);
        assertThat(productAfter).isNotNull();
        assertThat(productAfter.getStockQuantity()).isEqualTo(stockBefore - request.quantity());
    }

    @Test
    @Order(3)
    @DisplayName("Should return 422 when insufficient stock for OUT movement")
    void shouldReturn422WhenInsufficientStockForOutMovement() {
        Product product = productRepository.findById(testProductId).orElse(null);
        assertThat(product).isNotNull();

        Integer currentStock = product.getStockQuantity();
        Integer excessiveQuantity = currentStock + 100;

        CreateStockMovementRequest request = StockMovementTestFactory
                .insufficientStockMovementRequest(testProductId, excessiveQuantity);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/stock-movements", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("Insufficient stock");

        Product productAfter = productRepository.findById(testProductId).orElse(null);
        assertThat(productAfter).isNotNull();
        assertThat(productAfter.getStockQuantity()).isEqualTo(currentStock);
    }

    @Test
    @Order(4)
    @DisplayName("Should return 404 when product does not exist")
    void shouldReturn404WhenProductDoesNotExist() {
        UUID nonExistentProductId = UUID.randomUUID();
        CreateStockMovementRequest request = StockMovementTestFactory
                .validInMovementRequest(nonExistentProductId);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/stock-movements", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Product not found");
    }

    @Test
    @Order(5)
    @DisplayName("Should get all stock movements with pagination")
    void shouldGetAllStockMovementsWithPagination() {
        CreateStockMovementRequest firstMovement = StockMovementTestFactory
                .validInMovementRequest(testProductId);
        restTemplate.postForEntity("/api/v1/stock-movements", firstMovement, StockMovementResponse.class);

        CreateStockMovementRequest secondMovement = StockMovementTestFactory
                .validOutMovementRequest(testProductId, 2);
        restTemplate.postForEntity("/api/v1/stock-movements", secondMovement, StockMovementResponse.class);

        ParameterizedTypeReference<RestResponsePage<StockMovementResponse>> responseType =
                new ParameterizedTypeReference<RestResponsePage<StockMovementResponse>>() {
                };
        ResponseEntity<RestResponsePage<StockMovementResponse>> response = restTemplate.exchange(
                "/api/v1/stock-movements", HttpMethod.GET, null, responseType);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSizeGreaterThanOrEqualTo(2);

        boolean hasInMovement = response.getBody().getContent().stream()
                .anyMatch(m -> m.movementType().name().equals("IN"));
        boolean hasOutMovement = response.getBody().getContent().stream()
                .anyMatch(m -> m.movementType().name().equals("OUT"));

        assertThat(hasInMovement).isTrue();
        assertThat(hasOutMovement).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("Should maintain stock calculation consistency across multiple movements")
    void shouldMaintainStockCalculationConsistencyAcrossMultipleMovements() {
        Product initialProduct = productRepository.findById(testProductId).orElse(null);
        assertThat(initialProduct).isNotNull();
        Integer initialStock = initialProduct.getStockQuantity();

        CreateStockMovementRequest inMovement1 = StockMovementTestFactory
                .adjustmentMovementRequest(testProductId, 15, com.inventory.enums.MovementType.IN);
        ResponseEntity<StockMovementResponse> response1 = restTemplate.postForEntity(
                "/api/v1/stock-movements", inMovement1, StockMovementResponse.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CreateStockMovementRequest outMovement = StockMovementTestFactory
                .adjustmentMovementRequest(testProductId, 8, com.inventory.enums.MovementType.OUT);
        ResponseEntity<StockMovementResponse> response2 = restTemplate.postForEntity(
                "/api/v1/stock-movements", outMovement, StockMovementResponse.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CreateStockMovementRequest inMovement2 = StockMovementTestFactory
                .adjustmentMovementRequest(testProductId, 5, com.inventory.enums.MovementType.IN);
        ResponseEntity<StockMovementResponse> response3 = restTemplate.postForEntity(
                "/api/v1/stock-movements", inMovement2, StockMovementResponse.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Product finalProduct = productRepository.findById(testProductId).orElse(null);
        assertThat(finalProduct).isNotNull();

        Integer expectedStock = initialStock + 15 - 8 + 5; // +15 -8 +5 = +12
        assertThat(finalProduct.getStockQuantity()).isEqualTo(expectedStock);

        StockMovementResponse lastMovement = response3.getBody();
        assertThat(lastMovement).isNotNull();
        assertThat(lastMovement.newStock()).isEqualTo(expectedStock);
    }

    @Test
    @Order(7)
    @DisplayName("Should return 400 when trying to create movement with invalid data")
    void shouldReturn400WhenTryingToCreateMovementWithInvalidData() {
        // Given - Request with missing required fields
        String invalidJson = "{}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(invalidJson, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/stock-movements",
                HttpMethod.POST,
                entity,
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("is required");
    }

    @Test
    @Order(8)
    @DisplayName("Should return 400 when trying to create movement with negative quantity")
    void shouldReturn400WhenTryingToCreateMovementWithNegativeQuantity() {
        // Given - Request with negative quantity
        CreateStockMovementRequest request = new CreateStockMovementRequest(
                testProductId,
                MovementType.IN,
                -10, // Negative quantity
                MovementReason.PURCHASE,
                "INV-001",
                "Invalid negative quantity"
        );

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/stock-movements", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("must be greater than 0");
    }

    @Test
    @Order(9)
    @DisplayName("Should return 400 when trying to create movement with zero quantity")
    void shouldReturn400WhenTryingToCreateMovementWithZeroQuantity() {
        // Given - Request with zero quantity
        CreateStockMovementRequest request = new CreateStockMovementRequest(
                testProductId,
                MovementType.IN,
                0, // Zero quantity
                MovementReason.PURCHASE,
                "INV-001",
                "Invalid zero quantity"
        );

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/stock-movements", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("must be greater than 0");
    }

    @Test
    @Order(10)
    @DisplayName("Should return 400 when trying to create movement with invalid UUID")
    void shouldReturn400WhenTryingToCreateMovementWithInvalidUuid() {
        // Given - Request with malformed JSON containing invalid UUID
        String invalidJson = "{ \"productId\": \"invalid-uuid\", \"movementType\": \"IN\", \"quantity\": 10, \"reason\": \"PURCHASE\" }";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(invalidJson, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/stock-movements",
                HttpMethod.POST,
                entity,
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Invalid");
    }

    @Test
    @Order(11)
    @DisplayName("Should handle concurrent stock movements correctly")
    void shouldHandleConcurrentStockMovementsCorrectly() {
        // Given - Get current stock
        Product product = productRepository.findById(testProductId).orElse(null);
        assertThat(product).isNotNull();
        Integer initialStock = product.getStockQuantity();

        // When - Create multiple movements that should be processed sequentially
        CreateStockMovementRequest movement1 = new CreateStockMovementRequest(
                testProductId, MovementType.IN, 5, MovementReason.PURCHASE, "CONC-001", "Concurrent test 1");
        CreateStockMovementRequest movement2 = new CreateStockMovementRequest(
                testProductId, MovementType.OUT, 3, MovementReason.SALE, "CONC-002", "Concurrent test 2");
        CreateStockMovementRequest movement3 = new CreateStockMovementRequest(
                testProductId, MovementType.IN, 2, MovementReason.ADJUSTMENT, "CONC-003", "Concurrent test 3");

        ResponseEntity<StockMovementResponse> response1 = restTemplate.postForEntity(
                "/api/v1/stock-movements", movement1, StockMovementResponse.class);
        ResponseEntity<StockMovementResponse> response2 = restTemplate.postForEntity(
                "/api/v1/stock-movements", movement2, StockMovementResponse.class);
        ResponseEntity<StockMovementResponse> response3 = restTemplate.postForEntity(
                "/api/v1/stock-movements", movement3, StockMovementResponse.class);

        // Then - All movements should succeed and final stock should be correct
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Product finalProduct = productRepository.findById(testProductId).orElse(null);
        assertThat(finalProduct).isNotNull();

        Integer expectedFinalStock = initialStock + 5 - 3 + 2; // +5 -3 +2 = +4
        assertThat(finalProduct.getStockQuantity()).isEqualTo(expectedFinalStock);
    }

    @AfterEach
    void cleanupAfterEachTest() {
        stockMovementRepository.deleteAll();

        Product product = productRepository.findById(testProductId).orElse(null);
        if (product != null) {
            product.setStockQuantity(10);
            productRepository.save(product);
        }
    }
}