package com.hung.wms.service.impl;

import com.hung.wms.converter.UserConverter;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.model.request.auth.LoginRequest;
import com.hung.wms.model.request.auth.RegisterRequest;
import com.hung.wms.model.response.auth.TokenResponse;
import com.hung.wms.model.response.user.UserResponse;
import com.hung.wms.repository.UserRepository;
import com.hung.wms.repository.entity.UserEntity;
import com.hung.wms.service.AuthService;
import com.hung.wms.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String DEFAULT_ROLE = "STAFF";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ResourceDuplicateException("Username already exists: " + request.getUsername());
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ResourceDuplicateException("Email already exists: " + request.getEmail());

        UserEntity user = userConverter.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(DEFAULT_ROLE);
        return userConverter.toResponse(userRepository.save(user));
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        TokenResponse response = new TokenResponse();
        response.setAccessToken(jwtService.generateToken(userDetails));
        return response;
    }
}
