package com.hung.wms.exception;

import org.springframework.http.HttpStatus;

// 409: the current state does not allow the operation (e.g. purchase order already RECEIVED)
public class InvalidStateException extends AppException {
    public InvalidStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    // data: details for the client, e.g. the current status of the purchase order
    public InvalidStateException(String message, Object data) {
        super(HttpStatus.CONFLICT, message, data);
    }
}
