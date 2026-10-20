package com.hung.wms.repository.specification;

// Builds LIKE patterns for SpecificationUtils.contains / startsWith.
// - No lower(): the columns use the MySQL default collation utf8mb4_0900_ai_ci, which already ignores case
//   (and accents). lower(column) only forced a full scan because no index can be used on it.
// - Codes use a prefix pattern ("abc%") so MySQL can range-scan the unique index on the code column;
//   "%abc%" can never use a B-tree index.
// - %, _ and the escape char typed by the user are escaped, so they match literally and a leading %
//   cannot turn a prefix search back into a full scan.
final class LikePattern {
    static final char ESCAPE = '!';

    private LikePattern() {
    }

    static String contains(String value) {
        return "%" + escape(value.trim()) + "%";
    }

    static String startsWith(String value) {
        return escape(value.trim()) + "%";
    }

    private static String escape(String value) {
        return value
                .replace(String.valueOf(ESCAPE), "" + ESCAPE + ESCAPE)
                .replace("%", ESCAPE + "%")
                .replace("_", ESCAPE + "_");
    }
}
