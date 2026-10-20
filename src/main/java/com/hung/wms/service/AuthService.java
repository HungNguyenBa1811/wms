package com.hung.wms.service;

import com.hung.wms.dto.request.auth.LoginRequest;
import com.hung.wms.dto.request.auth.RegisterRequest;
import com.hung.wms.dto.response.auth.TokenResponse;
import com.hung.wms.dto.response.user.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
}
