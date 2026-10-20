package com.hung.wms.mapper;

import com.hung.wms.dto.request.product.ProductRequest;
import com.hung.wms.dto.response.product.ProductResponse;
import com.hung.wms.entity.ProductEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    @Autowired
    private ModelMapper modelMapper;

    public ProductEntity toEntity(ProductRequest request) {
        return modelMapper.map(request, ProductEntity.class);
    }

    public ProductResponse toResponse(ProductEntity product) {
        return modelMapper.map(product, ProductResponse.class);
    }
}
