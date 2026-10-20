package com.hung.wms.mapper;

import com.hung.wms.dto.request.category.CategoryRequest;
import com.hung.wms.dto.response.category.CategoryResponse;
import com.hung.wms.entity.CategoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    @Autowired
    private ModelMapper modelMapper;

    public CategoryEntity toEntity(CategoryRequest request) {
        return modelMapper.map(request, CategoryEntity.class);
    }

    public CategoryResponse toResponse(CategoryEntity categoryEntity) {
        return modelMapper.map(categoryEntity, CategoryResponse.class);
    }
}
