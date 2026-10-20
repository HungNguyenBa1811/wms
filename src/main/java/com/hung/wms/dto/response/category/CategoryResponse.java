package com.hung.wms.dto.response.category;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CategoryResponse {
    private String id, name, description;
    private LocalDateTime createdAt;
    // Username of the logged-in user who created the row (JPA auditing), null if created without login
    private String createdBy;
}
