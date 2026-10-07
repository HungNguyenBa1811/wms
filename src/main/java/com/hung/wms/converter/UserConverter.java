package com.hung.wms.converter;

import com.hung.wms.model.request.auth.RegisterRequest;
import com.hung.wms.model.response.user.UserResponse;
import com.hung.wms.repository.entity.UserEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {
    @Autowired
    private ModelMapper modelMapper;

    // password is copied as plain text here; the service hashes it before saving
    public UserEntity toEntity(RegisterRequest request) {
        return modelMapper.map(request, UserEntity.class);
    }

    public UserResponse toResponse(UserEntity user) {
        return modelMapper.map(user, UserResponse.class);
    }
}
