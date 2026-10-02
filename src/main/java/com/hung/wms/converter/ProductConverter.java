package com.hung.wms.converter;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.repository.entity.ProductEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {
    @Autowired
    private ModelMapper modelMapper;

    public ProductEntity toEntity(ProductRequest request) {
        return modelMapper.map(request, ProductEntity.class);
    }

    public ProductResponse toResponse(ProductEntity product) {
        return modelMapper.map(product, ProductResponse.class);
    }
}
