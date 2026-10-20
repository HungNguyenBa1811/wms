package com.hung.wms.api;

import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.model.request.auth.LoginRequest;
import com.hung.wms.model.request.auth.RegisterRequest;
import com.hung.wms.model.response.auth.TokenResponse;
import com.hung.wms.model.response.user.UserResponse;
import com.hung.wms.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// /api/auth/** is permitAll: no @WithMockUser here
@WebMvcTest(AuthAPI.class)
@DisplayName("AuthAPI (@WebMvcTest)")
class AuthAPITest extends ApiSliceTestSupport {
    private static final String REGISTER_BODY = """
            {"username": "new.user", "email": "new@example.com", "password": "password123"}
            """;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register -> 201, không trả password")
    void register() throws Exception {
        // given
        UserResponse user = new UserResponse();
        user.setId("user-1");
        user.setUsername("new.user");
        user.setRole("STAFF");
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        // when / then
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new.user"))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register trùng username -> 409")
    void registerDuplicate() throws Exception {
        // given
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ResourceDuplicateException("Username already exists: new.user"));

        // when / then
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(errorResponse(409, "Conflict", "Username already exists: new.user", "/api/auth/register"));
    }

    @Test
    @DisplayName("POST /api/auth/register password quá 72 byte UTF-8 -> 400")
    void registerPasswordTooLong() throws Exception {
        // given: 40 Vietnamese characters = 80 bytes in UTF-8
        String password = "ậ".repeat(40);
        String body = "{\"username\": \"new.user\", \"email\": \"new@example.com\", \"password\": \"" + password + "\"}";

        // when / then
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/auth/register"))
                .andExpect(jsonPath("$.detail[0]").value(startsWith("password: Password must be at most 72 bytes")));
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/login -> 200 TokenResponse tokenType Bearer")
    void login() throws Exception {
        // given
        TokenResponse token = new TokenResponse();
        token.setAccessToken("jwt-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(token);

        // when / then
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"admin\", \"password\": \"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/login sai password -> 401 message cố định")
    void loginBadCredentials() throws Exception {
        // given
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        // when / then
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"admin\", \"password\": \"wrong\"}"))
                .andExpect(errorResponse(401, "Unauthorized", "Invalid username or password", "/api/auth/login"));
    }

    @Test
    @DisplayName("POST /api/auth/login thiếu password -> 400")
    void loginMissingPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\": \"admin\"}"))
                .andExpect(errorResponse(400, "Bad Request", "Validation failed", "/api/auth/login"))
                .andExpect(jsonPath("$.detail[0]").value("password: Password is required"));
        verifyNoInteractions(authService);
    }
}
