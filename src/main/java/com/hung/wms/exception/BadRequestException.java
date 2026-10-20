package com.hung.wms.exception;

import org.springframework.http.HttpStatus;

// 400: input is valid in format but breaks a rule that needs DB data (Bean Validation covers pure input)
public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
