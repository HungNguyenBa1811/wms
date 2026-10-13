package com.hung.wms.enums;

// Stored without the ROLE_ prefix: UserDetailsServiceImpl calls .roles(), which adds it
public enum Role {
    ADMIN,
    STAFF
}
