package com.vibevault.productservice.specifications;

import com.vibevault.productservice.models.Category;
import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.UUID;

public class ProductSpecification {

    private static final char ESCAPE_CHAR = '\\';

    /**
     * Escapes LIKE pattern wildcards (%, _) and the escape character itself
     * so user input is treated literally.
     */
    private static String escapeLikePattern(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public static Specification<Product> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
    }

    public static Specification<Product> withQuery(String searchQuery) {
        return (root, query, criteriaBuilder) -> {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String escaped = escapeLikePattern(searchQuery.toLowerCase());
            String pattern = "%" + escaped + "%";
            // Note: description is a @Lob (CLOB) type which doesn't support lower() in MySQL
            // For full-text search including description, use Elasticsearch
            // Here we search on name only for MySQL compatibility
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern, ESCAPE_CHAR);
        };
    }

    public static Specification<Product> withMinPrice(Double minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("price").get("price"), minPrice);
        };
    }

    public static Specification<Product> withMaxPrice(Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("price").get("price"), maxPrice);
        };
    }

    public static Specification<Product> withCurrency(Currency currency) {
        return (root, query, criteriaBuilder) -> {
            if (currency == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("price").get("currency"), currency);
        };
    }

    public static Specification<Product> withCategoryId(UUID categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Product> withCategoryName(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(categoryJoin.get("name")),
                    categoryName.toLowerCase()
            );
        };
    }

    public static Specification<Product> withCreatedAfter(Date createdAfter) {
        return (root, query, criteriaBuilder) -> {
            if (createdAfter == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter);
        };
    }

    public static Specification<Product> withCreatedBefore(Date createdBefore) {
        return (root, query, criteriaBuilder) -> {
            if (createdBefore == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdBefore);
        };
    }

    public static Specification<Product> withNamePrefix(String prefix) {
        return (root, query, criteriaBuilder) -> {
            if (prefix == null || prefix.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String escaped = escapeLikePattern(prefix.toLowerCase());
            String pattern = escaped + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern, ESCAPE_CHAR);
        };
    }
}
