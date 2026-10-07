package com.hung.wms.exception;

import com.hung.wms.model.response.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceDuplicateException.class)
    public ResponseEntity<Object> handleResourceDuplicateException(
            ResourceDuplicateException ex,
            WebRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<Object> handleResourceInUseException(
            ResourceInUseException ex,
            WebRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<Object> handleInvalidStateException(
            InvalidStateException ex,
            WebRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(
            BadRequestException ex,
            WebRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }


    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Object> handlePropertyReferenceException(
            PropertyReferenceException ex, WebRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid sort field: " + ex.getPropertyName(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(
        BadCredentialsException ex,
        WebRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<String> detail = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (error.isBindingFailure())
                detail.add(error.getField() + ": invalid value '" + error.getRejectedValue() + "'");
            else
                detail.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            detail.add(error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", detail, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<String> detail = new ArrayList<>();
        if (ex.getCause() instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
            String field = toFieldPath(mismatch.getPath());
            if (mismatch instanceof InvalidFormatException invalidFormat)
                detail.add(field + ": invalid value '" + invalidFormat.getValue() + "'");
            else
                detail.add(field + ": invalid type");
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", detail, request);
    }

    private String toFieldPath(List<JacksonException.Reference> path) {
        StringBuilder field = new StringBuilder();
        for (JacksonException.Reference reference : path) {
            if (reference.getPropertyName() != null) {
                if (!field.isEmpty())
                    field.append('.');
                field.append(reference.getPropertyName());
            } else if (reference.getIndex() >= 0) {
                field.append('[').append(reference.getIndex()).append(']');
            }
        }
        return field.toString();
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, WebRequest request) {
        return buildResponse(status, message, new ArrayList<>(), request);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, List<String> detail, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(message);
        errorResponse.setDetail(detail);
        errorResponse.setPath(request.getDescription(false).replaceFirst("^uri=", ""));
        return new ResponseEntity<>(errorResponse, status);
    }
}
