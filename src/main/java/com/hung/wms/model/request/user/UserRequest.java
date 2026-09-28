package com.hung.wms.model.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private String username, email, password, role;
}
