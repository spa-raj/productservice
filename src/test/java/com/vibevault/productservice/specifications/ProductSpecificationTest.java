package com.vibevault.productservice.specifications;

import com.vibevault.productservice.models.Currency;
import com.vibevault.productservice.models.Product;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductSpecification.
 * These tests verify that specifications are created correctly and can be composed.
 * The actual SQL generation is tested implicitly through integration tests.
 */
class ProductSpecificationTest {

    // ==================== SPECIFICATION CREATION TESTS ====================

    @Test
    void notDeleted_shouldReturnNonNullSpecification() {
        Specification<Product> spec = ProductSpecification.notDeleted();
        assertNotNull(spec);
    }

    @Test
    void withQuery_shouldReturnNonNullSpecification_whenQueryIsProvided() {
        Specification<Product> spec = ProductSpecification.withQuery("test");
        assertNotNull(spec);
    }

    @Test
    void withQuery_shouldReturnNonNullSpecification_whenQueryIsNull() {
        Specification<Product> spec = ProductSpecification.withQuery(null);
        assertNotNull(spec);
    }

    @Test
    void withQuery_shouldReturnNonNullSpecification_whenQueryIsEmpty() {
        Specification<Product> spec = ProductSpecification.withQuery("");
        assertNotNull(spec);
    }

    @Test
    void withMinPrice_shouldReturnNonNullSpecification_whenPriceIsProvided() {
        Specification<Product> spec = ProductSpecification.withMinPrice(100.0);
        assertNotNull(spec);
    }

    @Test
    void withMinPrice_shouldReturnNonNullSpecification_whenPriceIsNull() {
        Specification<Product> spec = ProductSpecification.withMinPrice(null);
        assertNotNull(spec);
    }

    @Test
    void withMaxPrice_shouldReturnNonNullSpecification_whenPriceIsProvided() {
        Specification<Product> spec = ProductSpecification.withMaxPrice(500.0);
        assertNotNull(spec);
    }

    @Test
    void withMaxPrice_shouldReturnNonNullSpecification_whenPriceIsNull() {
        Specification<Product> spec = ProductSpecification.withMaxPrice(null);
        assertNotNull(spec);
    }

    @Test
    void withCurrency_shouldReturnNonNullSpecification_whenCurrencyIsProvided() {
        Specification<Product> spec = ProductSpecification.withCurrency(Currency.USD);
        assertNotNull(spec);
    }

    @Test
    void withCurrency_shouldReturnNonNullSpecification_whenCurrencyIsNull() {
        Specification<Product> spec = ProductSpecification.withCurrency(null);
        assertNotNull(spec);
    }

    @Test
    void withCategoryId_shouldReturnNonNullSpecification_whenIdIsProvided() {
        Specification<Product> spec = ProductSpecification.withCategoryId(UUID.randomUUID());
        assertNotNull(spec);
    }

    @Test
    void withCategoryId_shouldReturnNonNullSpecification_whenIdIsNull() {
        Specification<Product> spec = ProductSpecification.withCategoryId(null);
        assertNotNull(spec);
    }

    @Test
    void withCategoryName_shouldReturnNonNullSpecification_whenNameIsProvided() {
        Specification<Product> spec = ProductSpecification.withCategoryName("Electronics");
        assertNotNull(spec);
    }

    @Test
    void withCategoryName_shouldReturnNonNullSpecification_whenNameIsNull() {
        Specification<Product> spec = ProductSpecification.withCategoryName(null);
        assertNotNull(spec);
    }

    @Test
    void withCategoryName_shouldReturnNonNullSpecification_whenNameIsEmpty() {
        Specification<Product> spec = ProductSpecification.withCategoryName("");
        assertNotNull(spec);
    }

    @Test
    void withCreatedAfter_shouldReturnNonNullSpecification_whenDateIsProvided() {
        Specification<Product> spec = ProductSpecification.withCreatedAfter(new Date());
        assertNotNull(spec);
    }

    @Test
    void withCreatedAfter_shouldReturnNonNullSpecification_whenDateIsNull() {
        Specification<Product> spec = ProductSpecification.withCreatedAfter(null);
        assertNotNull(spec);
    }

    @Test
    void withCreatedBefore_shouldReturnNonNullSpecification_whenDateIsProvided() {
        Specification<Product> spec = ProductSpecification.withCreatedBefore(new Date());
        assertNotNull(spec);
    }

    @Test
    void withCreatedBefore_shouldReturnNonNullSpecification_whenDateIsNull() {
        Specification<Product> spec = ProductSpecification.withCreatedBefore(null);
        assertNotNull(spec);
    }

    @Test
    void withNamePrefix_shouldReturnNonNullSpecification_whenPrefixIsProvided() {
        Specification<Product> spec = ProductSpecification.withNamePrefix("iph");
        assertNotNull(spec);
    }

    @Test
    void withNamePrefix_shouldReturnNonNullSpecification_whenPrefixIsNull() {
        Specification<Product> spec = ProductSpecification.withNamePrefix(null);
        assertNotNull(spec);
    }

    @Test
    void withNamePrefix_shouldReturnNonNullSpecification_whenPrefixIsEmpty() {
        Specification<Product> spec = ProductSpecification.withNamePrefix("");
        assertNotNull(spec);
    }

    // ==================== SPECIFICATION COMPOSITION TESTS ====================

    @Test
    void specifications_canBeCombinedWithWhere() {
        Specification<Product> spec = Specification.where(ProductSpecification.notDeleted());
        assertNotNull(spec);
    }

    @Test
    void specifications_canBeCombinedWithAnd() {
        Specification<Product> spec = Specification
                .where(ProductSpecification.notDeleted())
                .and(ProductSpecification.withQuery("test"));
        assertNotNull(spec);
    }

    @Test
    void specifications_canCombineMultipleFilters() {
        Specification<Product> spec = Specification
                .where(ProductSpecification.notDeleted())
                .and(ProductSpecification.withQuery("iphone"))
                .and(ProductSpecification.withMinPrice(100.0))
                .and(ProductSpecification.withMaxPrice(500.0))
                .and(ProductSpecification.withCurrency(Currency.USD))
                .and(ProductSpecification.withCategoryName("Electronics"));
        assertNotNull(spec);
    }

    @Test
    void specifications_canCombineAllFilters() {
        UUID categoryId = UUID.randomUUID();
        Date now = new Date();
        Date yesterday = new Date(System.currentTimeMillis() - 86400000);

        Specification<Product> spec = Specification
                .where(ProductSpecification.notDeleted())
                .and(ProductSpecification.withQuery("test"))
                .and(ProductSpecification.withMinPrice(10.0))
                .and(ProductSpecification.withMaxPrice(1000.0))
                .and(ProductSpecification.withCurrency(Currency.USD))
                .and(ProductSpecification.withCategoryId(categoryId))
                .and(ProductSpecification.withCategoryName("Electronics"))
                .and(ProductSpecification.withCreatedAfter(yesterday))
                .and(ProductSpecification.withCreatedBefore(now))
                .and(ProductSpecification.withNamePrefix("test"));

        assertNotNull(spec);
    }

    @Test
    void specifications_handleAllNullValues() {
        // This should not throw any exceptions
        Specification<Product> spec = Specification
                .where(ProductSpecification.withQuery(null))
                .and(ProductSpecification.withMinPrice(null))
                .and(ProductSpecification.withMaxPrice(null))
                .and(ProductSpecification.withCurrency(null))
                .and(ProductSpecification.withCategoryId(null))
                .and(ProductSpecification.withCategoryName(null))
                .and(ProductSpecification.withCreatedAfter(null))
                .and(ProductSpecification.withCreatedBefore(null))
                .and(ProductSpecification.withNamePrefix(null));

        assertNotNull(spec);
    }

    @Test
    void specifications_canBeUsedWithOr() {
        Specification<Product> spec = Specification
                .where(ProductSpecification.withQuery("iphone"))
                .or(ProductSpecification.withQuery("samsung"));
        assertNotNull(spec);
    }

    @Test
    void specifications_canBeNegated() {
        Specification<Product> spec = Specification.not(ProductSpecification.notDeleted());
        assertNotNull(spec);
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void withQuery_shouldHandleSpecialCharacters() {
        Specification<Product> spec = ProductSpecification.withQuery("test@#$%^&*()");
        assertNotNull(spec);
    }

    @Test
    void withQuery_shouldHandleWhitespace() {
        Specification<Product> spec = ProductSpecification.withQuery("   ");
        assertNotNull(spec);
    }

    @Test
    void withMinPrice_shouldHandleZero() {
        Specification<Product> spec = ProductSpecification.withMinPrice(0.0);
        assertNotNull(spec);
    }

    @Test
    void withMaxPrice_shouldHandleZero() {
        Specification<Product> spec = ProductSpecification.withMaxPrice(0.0);
        assertNotNull(spec);
    }

    @Test
    void withMinPrice_shouldHandleLargeValue() {
        Specification<Product> spec = ProductSpecification.withMinPrice(Double.MAX_VALUE);
        assertNotNull(spec);
    }

    @Test
    void withCategoryName_shouldHandleWhitespace() {
        Specification<Product> spec = ProductSpecification.withCategoryName("   ");
        assertNotNull(spec);
    }

    @Test
    void withNamePrefix_shouldHandleSingleCharacter() {
        Specification<Product> spec = ProductSpecification.withNamePrefix("a");
        assertNotNull(spec);
    }

    @Test
    void withCreatedAfter_shouldHandleOldDate() {
        Date oldDate = new Date(0); // 1970
        Specification<Product> spec = ProductSpecification.withCreatedAfter(oldDate);
        assertNotNull(spec);
    }

    @Test
    void withCreatedBefore_shouldHandleFutureDate() {
        Date futureDate = new Date(System.currentTimeMillis() + 86400000L * 365);
        Specification<Product> spec = ProductSpecification.withCreatedBefore(futureDate);
        assertNotNull(spec);
    }
}
