package com.hung.wms.repository.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generic building blocks for the XxxSpecification.search(request) methods.
 * Every method returns Specification.unrestricted() (no WHERE condition) when the filter value is null or blank,
 * so search(request) can simply combine all filters with Specification.allOf(...).
 * Field names are entity attribute names; a dot walks into a relation ("category.id").
 */
public final class SpecificationUtils {
    private SpecificationUtils() {
    }

    /** Soft-deleted rows are hidden: isDeleted = false (entities extending SoftDeleteEntity). */
    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    /** LIKE '%value%' for free text (name, description, ...). See LikePattern: no lower(), wildcards escaped. */
    public static <T> Specification<T> contains(String field, String value) {
        if (isBlank(value)) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(path(root, field), LikePattern.contains(value), LikePattern.ESCAPE);
    }

    /** LIKE 'value%' for codes: MySQL can range-scan the unique index of the code column. */
    public static <T> Specification<T> startsWith(String field, String value) {
        if (isBlank(value)) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(path(root, field), LikePattern.startsWith(value), LikePattern.ESCAPE);
    }

    /** field = value, for ids ("warehouse.id") and enums. A blank String counts as "no filter". */
    public static <T> Specification<T> equal(String field, Object value) {
        if (value == null || (value instanceof String text && text.isBlank())) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(path(root, field), value);
    }

    /** from <= field <= to, each bound optional (e.g. priceFrom / priceTo). */
    public static <T, Y extends Comparable<? super Y>> Specification<T> range(String field, Y from, Y to) {
        Specification<T> lower = from == null
                ? Specification.unrestricted()
                : (root, query, cb) -> cb.greaterThanOrEqualTo(path(root, field), from);
        Specification<T> upper = to == null
                ? Specification.unrestricted()
                : (root, query, cb) -> cb.lessThanOrEqualTo(path(root, field), to);
        return Specification.allOf(lower, upper);
    }

    /** field < value (strict), e.g. expiryBefore. Rows where field is null never match. */
    public static <T, Y extends Comparable<? super Y>> Specification<T> lessThan(String field, Y value) {
        if (value == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.lessThan(path(root, field), value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static <Y> Path<Y> path(Root<?> root, String field) {
        Path<?> path = root;
        for (String part : field.split("[.]")) {
            path = path.get(part);
        }
        @SuppressWarnings("unchecked")
        Path<Y> typed = (Path<Y>) path;
        return typed;
    }
}
