package com.hung.wms.exception;

import org.springframework.http.HttpStatus;

// 404: a referenced resource does not exist (or is soft-deleted)
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
