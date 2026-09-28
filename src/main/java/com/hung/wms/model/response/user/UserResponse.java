package com.hung.wms.model.response.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private String id, username, email, role;
    private LocalDateTime createdAt;
}
