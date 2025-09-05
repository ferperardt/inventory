package com.inventory.specification;

import com.inventory.entity.Product;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@DisplayName("ProductSpecification Tests")
class ProductSpecificationTest {

    @Autowired
    private TestEntityManager testEntityManager;


    @BeforeEach
    void setUp() {
        Product iphonePro = createProduct("iPhone 15 Pro", "Latest Apple smartphone with advanced features",
                "IPHONE15PRO", BigDecimal.valueOf(1199.99), 50, 10, "electronics");

        Product samsungGalaxy = createProduct("Samsung Galaxy S24", "Android flagship phone",
                "GALAXY24", BigDecimal.valueOf(899.99), 25, 5, "electronics");

        Product gamingChair = createProduct("Gaming Chair Pro", "Ergonomic gaming chair",
                "CHAIR001", BigDecimal.valueOf(299.99), 15, 8, "furniture");

        Product wirelessHeadset = createProduct("Wireless Headset", "Bluetooth wireless headset",
                "HEADSET01", BigDecimal.valueOf(79.99), 3, 5, "audio");

        testEntityManager.persistAndFlush(iphonePro);
        testEntityManager.persistAndFlush(samsungGalaxy);
        testEntityManager.persistAndFlush(gamingChair);
        testEntityManager.persistAndFlush(wirelessHeadset);
    }

    private Product createProduct(String name, String description, String sku, BigDecimal price,
                                  int stockQuantity, int minStockLevel, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setSku(sku);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setMinStockLevel(minStockLevel);
        product.setCategory(category);
        return product;
    }

    @Nested
    @DisplayName("isActive() Tests")
    class IsActiveTests {

        @Test
        @DisplayName("Should return only active products")
        void shouldReturnOnlyActiveProducts() {
            List<Product> results = findWithSpecification(ProductSpecification.isActive());

            assertThat(results)
                    .hasSize(4)
                    .allMatch(Product::getActive);
        }
    }

    @Nested
    @DisplayName("hasName() Tests")
    class HasNameTests {

        @Test
        @DisplayName("Should find products by exact name")
        void shouldFindProductsByExactName() {
            List<Product> results = findWithSpecification(ProductSpecification.hasName("iPhone 15 Pro"));

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getName()).isEqualTo("iPhone 15 Pro"));
        }

        @Test
        @DisplayName("Should find products by partial name (case-insensitive)")
        void shouldFindProductsByPartialNameCaseInsensitive() {
            List<Product> results = findWithSpecification(ProductSpecification.hasName("iphone"));

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getName()).containsIgnoringCase("iphone"));
        }

        @Test
        @DisplayName("Should find products by partial name containing 'gaming'")
        void shouldFindProductsByPartialNameContainingGaming() {
            List<Product> results = findWithSpecification(ProductSpecification.hasName("gaming"));

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getName()).containsIgnoringCase("gaming"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should return all products when name is null, empty or whitespace")
        void shouldReturnAllProductsWhenNameIsNullEmptyOrWhitespace(String name) {
            List<Product> results = findWithSpecification(ProductSpecification.hasName(name));

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should return empty list when name not found")
        void shouldReturnEmptyListWhenNameNotFound() {
            List<Product> results = findWithSpecification(ProductSpecification.hasName("NonExistentProduct"));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasCategory() Tests")
    class HasCategoryTests {

        @Test
        @DisplayName("Should find products by exact category")
        void shouldFindProductsByExactCategory() {
            List<Product> results = findWithSpecification(ProductSpecification.hasCategory("electronics"));

            assertThat(results)
                    .hasSize(2)
                    .allMatch(product -> product.getCategory().equals("electronics"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Should return all products when category is null, empty or whitespace")
        void shouldReturnAllProductsWhenCategoryIsNullEmptyOrWhitespace(String category) {
            List<Product> results = findWithSpecification(ProductSpecification.hasCategory(category));

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should return empty list when category not found")
        void shouldReturnEmptyListWhenCategoryNotFound() {
            List<Product> results = findWithSpecification(ProductSpecification.hasCategory("nonexistent"));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasSku() Tests")
    class HasSkuTests {

        @Test
        @DisplayName("Should find products by exact SKU")
        void shouldFindProductsByExactSku() {
            List<Product> results = findWithSpecification(ProductSpecification.hasSku("IPHONE15PRO"));

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getSku()).isEqualTo("IPHONE15PRO"));
        }

        @Test
        @DisplayName("Should find products by partial SKU (case-insensitive)")
        void shouldFindProductsByPartialSkuCaseInsensitive() {
            List<Product> results = findWithSpecification(ProductSpecification.hasSku("galaxy"));

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getSku()).containsIgnoringCase("galaxy"));
        }

        @Test
        @DisplayName("Should return all products when SKU is null")
        void shouldReturnAllProductsWhenSkuIsNull() {
            List<Product> results = findWithSpecification(ProductSpecification.hasSku(null));

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should return empty list when SKU not found")
        void shouldReturnEmptyListWhenSkuNotFound() {
            List<Product> results = findWithSpecification(ProductSpecification.hasSku("NONEXISTENT"));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasPriceBetween() Tests")
    class HasPriceBetweenTests {

        @Test
        @DisplayName("Should find products within price range")
        void shouldFindProductsWithinPriceRange() {
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasPriceBetween(BigDecimal.valueOf(200), BigDecimal.valueOf(1000))
            );

            assertThat(results)
                    .hasSize(2) // Gaming Chair (299.99) and Samsung (899.99)
                    .allMatch(product ->
                            product.getPrice().compareTo(BigDecimal.valueOf(200)) >= 0 &&
                                    product.getPrice().compareTo(BigDecimal.valueOf(1000)) <= 0
                    );
        }

        @Test
        @DisplayName("Should find products with minimum price only")
        void shouldFindProductsWithMinimumPriceOnly() {
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasPriceBetween(BigDecimal.valueOf(500), null)
            );

            assertThat(results)
                    .hasSize(2) // Samsung (899.99) and iPhone (1199.99)
                    .allMatch(product -> product.getPrice().compareTo(BigDecimal.valueOf(500)) >= 0);
        }

        @Test
        @DisplayName("Should find products with maximum price only")
        void shouldFindProductsWithMaximumPriceOnly() {
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasPriceBetween(null, BigDecimal.valueOf(300))
            );

            assertThat(results)
                    .hasSize(2) // Gaming Chair (299.99) and Headset (79.99)
                    .allMatch(product -> product.getPrice().compareTo(BigDecimal.valueOf(300)) <= 0);
        }

        @Test
        @DisplayName("Should return all products when both prices are null")
        void shouldReturnAllProductsWhenBothPricesAreNull() {
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasPriceBetween(null, null)
            );

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should handle edge case where min equals max")
        void shouldHandleEdgeCaseWhereMinEqualsMax() {
            BigDecimal exactPrice = BigDecimal.valueOf(299.99);
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasPriceBetween(exactPrice, exactPrice)
            );

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getPrice()).isEqualByComparingTo(exactPrice));
        }
    }

    @Nested
    @DisplayName("hasStockQuantityBetween() Tests")
    class HasStockQuantityBetweenTests {

        @Test
        @DisplayName("Should find products within stock range")
        void shouldFindProductsWithinStockRange() {
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasStockQuantityBetween(10, 30)
            );

            assertThat(results)
                    .hasSize(2) // Samsung (25) and Gaming Chair (15)
                    .allMatch(product ->
                            product.getStockQuantity() >= 10 && product.getStockQuantity() <= 30
                    );
        }

        @Test
        @DisplayName("Should find products with minimum stock only")
        void shouldFindProductsWithMinimumStockOnly() {
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasStockQuantityBetween(20, null)
            );

            assertThat(results)
                    .hasSize(2) // iPhone (50) and Samsung (25)
                    .allMatch(product -> product.getStockQuantity() >= 20);
        }

        @Test
        @DisplayName("Should return all products when both stock values are null")
        void shouldReturnAllProductsWhenBothStockValuesAreNull() {
            List<Product> results = findWithSpecification(
                    ProductSpecification.hasStockQuantityBetween(null, null)
            );

            assertThat(results).hasSize(4);
        }
    }

    @Nested
    @DisplayName("isLowStock() Tests")
    class IsLowStockTests {

        @Test
        @DisplayName("Should find products with low stock")
        void shouldFindProductsWithLowStock() {
            List<Product> results = findWithSpecification(ProductSpecification.isLowStock());

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> {
                        assertThat(product.getStockQuantity()).isLessThanOrEqualTo(product.getMinStockLevel());
                        assertThat(product.getName()).isEqualTo("Wireless Headset");
                    });
        }
    }

    @Nested
    @DisplayName("hasDescription() Tests")
    class HasDescriptionTests {

        @Test
        @DisplayName("Should find products by description containing text")
        void shouldFindProductsByDescriptionContainingText() {
            List<Product> results = findWithSpecification(ProductSpecification.hasDescription("Apple"));

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getDescription()).containsIgnoringCase("Apple"));
        }

        @Test
        @DisplayName("Should find products by description (case-insensitive)")
        void shouldFindProductsByDescriptionCaseInsensitive() {
            List<Product> results = findWithSpecification(ProductSpecification.hasDescription("ergonomic"));

            assertThat(results)
                    .hasSize(1)
                    .first()
                    .satisfies(product -> assertThat(product.getDescription()).containsIgnoringCase("ergonomic"));
        }

        @Test
        @DisplayName("Should return all products when description is null")
        void shouldReturnAllProductsWhenDescriptionIsNull() {
            List<Product> results = findWithSpecification(ProductSpecification.hasDescription(null));

            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should return empty list when description not found")
        void shouldReturnEmptyListWhenDescriptionNotFound() {
            List<Product> results = findWithSpecification(ProductSpecification.hasDescription("NonExistentDescription"));

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Combined Specifications Tests")
    class CombinedSpecificationsTests {

        @Test
        @DisplayName("Should combine specifications with AND")
        void shouldCombineSpecificationsWithAnd() {
            Specification<Product> combinedSpec = ProductSpecification.isActive()
                    .and(ProductSpecification.hasCategory("electronics"))
                    .and(ProductSpecification.hasPriceBetween(BigDecimal.valueOf(800), BigDecimal.valueOf(1200)));

            List<Product> results = findWithSpecification(combinedSpec);

            assertThat(results)
                    .hasSize(2) // iPhone (1199.99) and Samsung (899.99)
                    .allMatch(Product::getActive)
                    .allMatch(product -> product.getCategory().equals("electronics"))
                    .allMatch(product ->
                            product.getPrice().compareTo(BigDecimal.valueOf(800)) >= 0 &&
                                    product.getPrice().compareTo(BigDecimal.valueOf(1200)) <= 0
                    );
        }

        @Test
        @DisplayName("Should combine specifications with OR")
        void shouldCombineSpecificationsWithOr() {
            Specification<Product> combinedSpec = ProductSpecification.hasCategory("audio")
                    .or(ProductSpecification.isLowStock());

            List<Product> results = findWithSpecification(combinedSpec);

            assertThat(results)
                    .hasSize(1) // Wireless Headset (both audio category and low stock)
                    .first()
                    .satisfies(product -> {
                        assertThat(product.getCategory()).isEqualTo("audio");
                        assertThat(product.getStockQuantity()).isLessThanOrEqualTo(product.getMinStockLevel());
                    });
        }
    }

    private List<Product> findWithSpecification(Specification<Product> specification) {
        CriteriaBuilder cb = testEntityManager.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        Predicate predicate = specification.toPredicate(root, query, cb);
        query.where(predicate);

        TypedQuery<Product> typedQuery = testEntityManager.getEntityManager().createQuery(query);
        return typedQuery.getResultList();
    }
}