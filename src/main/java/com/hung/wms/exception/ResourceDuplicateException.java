package com.hung.wms.exception;

import org.springframework.http.HttpStatus;

// 409: a unique value is already used (code, name, username, ...)
public class ResourceDuplicateException extends AppException {
    public ResourceDuplicateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
