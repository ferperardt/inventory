package com.inventory.specification;

import com.inventory.entity.Supplier;
import com.inventory.enums.SupplierStatus;
import com.inventory.enums.SupplierType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class SupplierSpecification {

    public static Specification<Supplier> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("active"));
    }

    public static Specification<Supplier> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Supplier> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Supplier> hasCity(String city) {
        return (root, query, criteriaBuilder) -> {
            if (city == null || city.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("address").get("city")),
                    "%" + city.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Supplier> hasCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            if (country == null || country.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("address").get("country")),
                    country.toUpperCase()
            );
        };
    }

    public static Specification<Supplier> hasStatus(SupplierStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Supplier> hasSupplierType(SupplierType supplierType) {
        return (root, query, criteriaBuilder) -> {
            if (supplierType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("supplierType"), supplierType);
        };
    }

    public static Specification<Supplier> hasRatingBetween(BigDecimal minRating, BigDecimal maxRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null && maxRating == null) {
                return criteriaBuilder.conjunction();
            }
            if (minRating != null && maxRating != null) {
                return criteriaBuilder.between(root.get("rating"), minRating, maxRating);
            }
            if (minRating != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating);
        };
    }

    public static Specification<Supplier> hasDeliveryDaysBetween(Integer minDays, Integer maxDays) {
        return (root, query, criteriaBuilder) -> {
            if (minDays == null && maxDays == null) {
                return criteriaBuilder.conjunction();
            }
            if (minDays != null && maxDays != null) {
                return criteriaBuilder.between(root.get("averageDeliveryDays"), minDays, maxDays);
            }
            if (minDays != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("averageDeliveryDays"), minDays);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("averageDeliveryDays"), maxDays);
        };
    }
}