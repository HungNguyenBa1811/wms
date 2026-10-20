package com.hung.wms.exception;

import org.springframework.http.HttpStatus;

// 409: cannot delete a resource that is still used (stock, pending purchase order, products)
public class ResourceInUseException extends AppException {
    public ResourceInUseException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
