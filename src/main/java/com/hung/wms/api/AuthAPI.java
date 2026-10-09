package com.hung.wms.api;

import com.hung.wms.model.request.auth.LoginRequest;
import com.hung.wms.model.request.auth.RegisterRequest;
import com.hung.wms.model.response.auth.TokenResponse;
import com.hung.wms.model.response.user.UserResponse;
import com.hung.wms.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
public class AuthAPI {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse result = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse result = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
