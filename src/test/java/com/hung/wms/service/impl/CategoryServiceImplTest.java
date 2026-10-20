package com.hung.wms.service.impl;

import com.hung.wms.mapper.CategoryMapper;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.dto.request.category.CategoryRequest;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.entity.CategoryEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl")
class CategoryServiceImplTest {
    private static final String CATEGORY_ID = "category-1";

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("createCategory: trùng name -> ResourceDuplicateException (409), không lưu")
    void createDuplicateName() {
        // given
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> categoryService.createCategory(request("Electronics")))
                .isInstanceOf(ResourceDuplicateException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Nested
    @DisplayName("updateCategory")
    class Update {
        @Test
        @DisplayName("không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, request("New")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("đổi sang name của category khác -> ResourceDuplicateException (409)")
        void duplicateName() {
            // given
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.of(category()));
            when(categoryRepository.existsByNameAndIdNot("Furniture", CATEGORY_ID)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> categoryService.updateCategory(CATEGORY_ID, request("Furniture")))
                    .isInstanceOf(ResourceDuplicateException.class);
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("không gửi name (partial update) -> bỏ qua check trùng")
        void partialUpdateSkipsDuplicateCheck() {
            // given
            CategoryEntity entity = category();
            CategoryRequest request = request(null);
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.of(entity));
            when(categoryRepository.save(entity)).thenReturn(entity);

            // when
            categoryService.updateCategory(CATEGORY_ID, request);

            // then
            verify(categoryRepository, never()).existsByNameAndIdNot(anyString(), anyString());
            verify(modelMapper).map(request, entity);
        }
    }

    @Nested
    @DisplayName("deleteCategoryById")
    class Delete {
        @Test
        @DisplayName("không tồn tại hoặc đã xóa -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> categoryService.deleteCategoryById(CATEGORY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("còn product chưa xóa -> ResourceInUseException (409), không xóa")
        void stillHasProducts() {
            // given
            CategoryEntity entity = category();
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.of(entity));
            when(productRepository.existsByCategory_IdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> categoryService.deleteCategoryById(CATEGORY_ID))
                    .isInstanceOf(ResourceInUseException.class);
            assertThat(entity.getIsDeleted()).isFalse();
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("không còn product -> xóa mềm (isDeleted = true)")
        void softDeletes() {
            // given
            CategoryEntity entity = category();
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.of(entity));
            when(productRepository.existsByCategory_IdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(false);

            // when
            categoryService.deleteCategoryById(CATEGORY_ID);

            // then
            assertThat(entity.getIsDeleted()).isTrue();
            verify(categoryRepository).save(entity);
        }
    }

    private static CategoryRequest request(String name) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        return request;
    }

    private static CategoryEntity category() {
        CategoryEntity category = new CategoryEntity();
        category.setId(CATEGORY_ID);
        category.setName("Electronics");
        return category;
    }
}
