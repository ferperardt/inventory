package com.inventory.integration.controller;

import com.inventory.dto.request.CreateSupplierRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import com.inventory.integration.fixtures.ProductTestFactory;
import com.inventory.integration.fixtures.RestResponsePage;
import com.inventory.integration.fixtures.SupplierTestFactory;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupplierControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    private UUID testSupplierId;


    @BeforeAll
    void setupTestData() {
        Supplier supplier = SupplierTestFactory.validSupplierEntity("Integration Test Supplier");
        Supplier savedSupplier = supplierRepository.save(supplier);

        testSupplierId = savedSupplier.getId();
    }

    @Test
    @Order(1)
    @DisplayName("Should create supplier successfully")
    void shouldCreateSupplierSuccessfully() {
        CreateSupplierRequest request = SupplierTestFactory.validSupplierRequest();

        ResponseEntity<SupplierResponse> response = restTemplate.postForEntity(
                "/api/v1/suppliers", request, SupplierResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo(request.name());
        assertThat(response.getBody().businessId()).isEqualTo(request.businessId());
        assertThat(response.getBody().email()).isEqualTo(request.email());
        assertThat(response.getBody().status()).isEqualTo(request.status());

        Supplier savedSupplier = supplierRepository.findById(response.getBody().id()).orElse(null);
        assertThat(savedSupplier).isNotNull();
        assertThat(savedSupplier.getActive()).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Should return 409 when business ID already exists")
    void shouldReturn409WhenBusinessIdAlreadyExists() {
        CreateSupplierRequest firstRequest = SupplierTestFactory.validSupplierRequest();
        restTemplate.postForEntity("/api/v1/suppliers", firstRequest, SupplierResponse.class);

        CreateSupplierRequest duplicateRequest = new CreateSupplierRequest(
                "Another Supplier Name",
                firstRequest.businessId(), // Same business ID
                SupplierStatus.ACTIVE,
                "another@email.com",
                "+1-555-0100",
                "Jane Doe",
                firstRequest.address(),
                "NET15",
                5,
                SupplierType.INTERNATIONAL,
                "Another supplier",
                BigDecimal.valueOf(3.0)
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/suppliers", duplicateRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("already exists");
    }

    @Test
    @Order(3)
    @DisplayName("Should get supplier products via n:n relationship")
    void shouldGetSupplierProductsViaNtoNRelationship() {
        var productRequest = ProductTestFactory.customProductRequest(
                "Supplier Product Test",
                "SUPPLIER-PROD-" + System.currentTimeMillis(),
                testSupplierId
        );

        ResponseEntity<Object> createResponse = restTemplate.postForEntity(
                "/api/v1/products", productRequest, Object.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String url = "/api/v1/suppliers/" + testSupplierId + "/products";

        ParameterizedTypeReference<RestResponsePage<ProductResponse>> responseType = 
            new ParameterizedTypeReference<RestResponsePage<ProductResponse>>() {};
        ResponseEntity<RestResponsePage<ProductResponse>> response = restTemplate.exchange(
            url, HttpMethod.GET, null, responseType);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSizeGreaterThanOrEqualTo(1);
        
        boolean productFound = response.getBody().getContent().stream()
            .anyMatch(p -> p.name().equals(productRequest.name()) && 
                          p.sku().equals(productRequest.sku()));
        assertThat(productFound).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("Should search suppliers with complex filters")
    void shouldSearchSuppliersWithComplexFilters() {
        Supplier domesticSupplier = SupplierTestFactory.validSupplierEntity("Domestic Supplier");
        domesticSupplier.setSupplierType(SupplierType.DOMESTIC);
        domesticSupplier.setRating(BigDecimal.valueOf(4.8));
        domesticSupplier.setAverageDeliveryDays(3);
        supplierRepository.save(domesticSupplier);

        Supplier internationalSupplier = SupplierTestFactory.validSupplierEntity("International Supplier");
        internationalSupplier.setSupplierType(SupplierType.INTERNATIONAL);
        internationalSupplier.setRating(BigDecimal.valueOf(3.2));
        internationalSupplier.setAverageDeliveryDays(14);
        supplierRepository.save(internationalSupplier);

        String searchUrl = "/api/v1/suppliers/search" +
                "?supplierType=DOMESTIC" +
                "&minRating=4.0" +
                "&maxDeliveryDays=7";

        ParameterizedTypeReference<RestResponsePage<SupplierResponse>> responseType = 
            new ParameterizedTypeReference<RestResponsePage<SupplierResponse>>() {};
        ResponseEntity<RestResponsePage<SupplierResponse>> response = restTemplate.exchange(
            searchUrl, HttpMethod.GET, null, responseType);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSizeGreaterThanOrEqualTo(1);
        
        boolean domesticSupplierFound = response.getBody().getContent().stream()
            .anyMatch(s -> s.name().equals("Domestic Supplier"));
        boolean internationalSupplierFound = response.getBody().getContent().stream()
            .anyMatch(s -> s.name().equals("International Supplier"));
        
        assertThat(domesticSupplierFound).isTrue();
        assertThat(internationalSupplierFound).isFalse();
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 when getting products of non-existent supplier")
    void shouldReturn404WhenGettingProductsOfNonExistentSupplier() {
        UUID nonExistentId = UUID.randomUUID();
        String url = "/api/v1/suppliers/" + nonExistentId + "/products";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Supplier not found");
    }

    @AfterEach
    void cleanupAfterEachTest() {
        stockMovementRepository.deleteAll();
        productRepository.deleteAll();
        // Keep suppliers for stability across tests
    }

}