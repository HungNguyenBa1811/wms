package com.hung.wms.model.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Only check presence here: length / format rules belong to register,
// so accounts created before a rule change can still log in
@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
