package com.hung.wms.service;

import com.hung.wms.model.request.auth.LoginRequest;
import com.hung.wms.model.request.auth.RegisterRequest;
import com.hung.wms.model.response.auth.TokenResponse;
import com.hung.wms.model.response.user.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);
}
