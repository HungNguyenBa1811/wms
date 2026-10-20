package com.hung.wms.repository.specification;

import com.hung.wms.entity.CategoryEntity;
import com.hung.wms.entity.ProductEntity;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Runs the generated SQL on H2 (profile "test"), each test rolls back. Same context as WmsApplicationTests (cached)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("SpecificationUtils (H2)")
class SpecificationUtilsTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryEntity category;

    @BeforeEach
    void seed() {
        category = new CategoryEntity();
        category.setName("Spec test category");
        category = categoryRepository.save(category);
        product("SPEC-001", "Fresh milk", "10.00", LocalDate.of(2026, 11, 1), false);
        product("SPEC-002", "Milk 100% fat", "20.00", null, false);
        product("XSPEC-003", "Old milk", "30.00", LocalDate.of(2026, 10, 1), true);
    }

    @Test
    @DisplayName("null / blank -> không lọc")
    void blankValuesAreUnrestricted() {
        assertThat(codes(Specification.allOf(
                SpecificationUtils.contains("name", "  "),
                SpecificationUtils.startsWith("productCode", null),
                SpecificationUtils.equal("category.id", ""),
                SpecificationUtils.range("price", null, null),
                SpecificationUtils.lessThan("expiryDate", null))))
                .containsExactlyInAnyOrder("SPEC-001", "SPEC-002", "XSPEC-003");
    }

    @Test
    @DisplayName("notDeleted bỏ dòng isDeleted = true")
    void notDeleted() {
        assertThat(codes(SpecificationUtils.notDeleted())).containsExactlyInAnyOrder("SPEC-001", "SPEC-002");
    }

    @Test
    @DisplayName("startsWith là tiền tố: SPEC khớp SPEC-001, không khớp XSPEC-003")
    void startsWithIsPrefix() {
        assertThat(codes(SpecificationUtils.startsWith("productCode", "SPEC"))).containsExactlyInAnyOrder("SPEC-001", "SPEC-002");
    }

    @Test
    // Case-insensitive matching comes from the MySQL collation, not from SpecificationUtils: H2 here is case-sensitive
    @DisplayName("contains: % trong giá trị là ký tự thường, không phải wildcard")
    void containsEscapesWildcards() {
        assertThat(codes(SpecificationUtils.contains("name", "ilk"))).containsExactlyInAnyOrder("SPEC-001", "SPEC-002", "XSPEC-003");
        assertThat(codes(SpecificationUtils.contains("name", "100%"))).containsExactly("SPEC-002");
        assertThat(codes(SpecificationUtils.contains("name", "%"))).containsExactly("SPEC-002");
    }

    @Test
    @DisplayName("equal theo quan hệ (category.id), range 2 đầu, lessThan bỏ dòng null")
    void equalRangeLessThan() {
        assertThat(codes(SpecificationUtils.equal("category.id", category.getId()))).hasSize(3);
        assertThat(codes(SpecificationUtils.range("price", new BigDecimal("15"), null))).containsExactlyInAnyOrder("SPEC-002", "XSPEC-003");
        assertThat(codes(SpecificationUtils.range("price", new BigDecimal("15"), new BigDecimal("20")))).containsExactly("SPEC-002");
        assertThat(codes(SpecificationUtils.lessThan("expiryDate", LocalDate.of(2026, 11, 1)))).containsExactly("XSPEC-003");
    }

    private void product(String code, String name, String price, LocalDate expiryDate, boolean deleted) {
        ProductEntity product = new ProductEntity();
        product.setProductCode(code);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setCategory(category);
        product.setImportedAt(LocalDate.of(2026, 9, 1));
        product.setExpiryDate(expiryDate);
        product.setIsDeleted(deleted);
        productRepository.save(product);
    }

    // Only the rows created by this test: the H2 database is shared with other @SpringBootTest classes
    private List<String> codes(Specification<ProductEntity> spec) {
        return productRepository.findAll(Specification.allOf(spec, SpecificationUtils.equal("category.id", category.getId())))
                .stream().map(ProductEntity::getProductCode).toList();
    }
}
