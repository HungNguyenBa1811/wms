package com.hung.wms.dto.response.supplier;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SupplierResponse {
    private String id, name, phone, email, address;
    private LocalDateTime createdAt;
    // Username of the logged-in user who created the row (JPA auditing), null if created without login
    private String createdBy;
}
