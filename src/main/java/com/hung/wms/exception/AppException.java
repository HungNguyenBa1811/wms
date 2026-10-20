package com.hung.wms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Generic business exception: carries the HTTP status, the message and optional data for the client.
 * GlobalExceptionHandler turns every AppException into an ErrorResponse with this status
 * ({@code data} is only written when not null).
 * The 5 subclasses only fix the status, so call sites stay readable: {@code throw new ResourceNotFoundException("...")}.
 * A new case with an existing status needs no new class: {@code throw new AppException(HttpStatus.XXX, "...")}.
 */
@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final transient Object data;

    public AppException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public AppException(HttpStatus status, String message, Object data) {
        super(message);
        this.status = status;
        this.data = data;
    }
}
