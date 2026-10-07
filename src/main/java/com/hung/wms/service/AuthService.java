package com.hung.wms.service;

import com.hung.wms.model.request.auth.LoginRequest;
import com.hung.wms.model.request.auth.RegisterRequest;
import com.hung.wms.model.response.user.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    // TODO JWT: return a token response instead of the user
    UserResponse login(LoginRequest request);
}
