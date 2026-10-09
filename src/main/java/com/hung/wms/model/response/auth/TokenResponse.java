package com.hung.wms.model.response.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
    private String accessToken;

    // Client sends it back as "Authorization: Bearer <accessToken>"
    private String tokenType = "Bearer";
}
