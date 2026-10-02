package com.hung.wms.config;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.repository.entity.ProductEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        // categoryId is resolved to a managed CategoryEntity in the service, not by the mapper
        modelMapper.typeMap(ProductRequest.class, ProductEntity.class)
                .addMappings(m -> m.skip(ProductEntity::setCategory));
        modelMapper.typeMap(ProductEntity.class, ProductResponse.class)
                .addMapping(src -> src.getCategory().getId(), ProductResponse::setCategoryId);

        return modelMapper;
    }
}
