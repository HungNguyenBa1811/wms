package com.hung.wms.converter;

import com.hung.wms.model.request.category.CategoryRequest;
import com.hung.wms.model.response.category.CategoryResponse;
import com.hung.wms.repository.entity.CategoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter {
    @Autowired
    private ModelMapper modelMapper;

    public CategoryEntity toEntity(CategoryRequest request) {
        return modelMapper.map(request, CategoryEntity.class);
    }

    public CategoryResponse toResponse(CategoryEntity categoryEntity) {
        return modelMapper.map(categoryEntity, CategoryResponse.class);
    }
}
