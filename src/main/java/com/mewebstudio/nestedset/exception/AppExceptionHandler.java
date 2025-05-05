package com.mewebstudio.nestedset.exception;

import com.mewebstudio.nestedset.dto.response.DetailedErrorResponse;
import com.mewebstudio.nestedset.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
            "Method " + e.getMethod() + " is not supported for this request. Supported methods are: " + e.getSupportedHttpMethods());
    }

    @ExceptionHandler({
        BadRequestException.class,
        MultipartException.class,
        MissingServletRequestPartException.class,
        HttpMediaTypeNotSupportedException.class,
        MethodArgumentTypeMismatchException.class,
        IllegalArgumentException.class,
        InvalidDataAccessApiUsageException.class,
        ConstraintViolationException.class,
        MissingRequestHeaderException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestException(Exception e) {
        return build(HttpStatus.BAD_REQUEST, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception e) {
        return build(HttpStatus.NOT_FOUND, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
    }

    /**
     * Build error response with errors map.
     *
     * @param httpStatus HttpStatus enum to response status field
     * @param message    String for response message field
     * @param errors     Map for response errors field
     * @return ResponseEntity
     */
    private ResponseEntity<ErrorResponse> build(HttpStatus httpStatus, String message, Map<String, String> errors) {
        ErrorResponse response = (errors.isEmpty()) ?
            new ErrorResponse(message) :
            new DetailedErrorResponse(message, errors);
        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Build error response without errors map.
     *
     * @param httpStatus HttpStatus enum to response status field
     * @param message    String for response message field
     * @return ResponseEntity
     */
    private ResponseEntity<ErrorResponse> build(HttpStatus httpStatus, String message) {
        return build(httpStatus, message, new HashMap<>());
    }
}
