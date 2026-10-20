package com.hung.wms.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.hung.wms.validation.MaxBytes;
import lombok.Getter;
import lombok.Setter;

// No role field: the server decides the role, never trust it from the client
@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username may only contain letters, digits, '.', '_' and '-'")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    // BCrypt accepts at most 72 bytes (not characters) and Spring Security throws on longer input.
    // @Size counts characters, so a non-ASCII password (e.g. Vietnamese) can pass @Size yet exceed 72 bytes;
    // @MaxBytes checks the UTF-8 byte length so the encoder never sees an oversized password.
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @MaxBytes(value = 72, message = "Password must be at most 72 bytes in UTF-8 (72 ASCII characters, fewer if accented)")
    private String password;
}
