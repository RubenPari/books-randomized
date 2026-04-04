package dev.rubenpari.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that translates application-level exceptions
 * into consistent JSON error responses with HTTP 400 status.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** Catches business-logic errors thrown as {@link IllegalStateException}. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Catches Bean Validation failures from {@code @Valid} annotated request bodies. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Catches external API errors and returns a user-friendly 502 response. */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleExternalApiError(RestClientResponseException ex) {
        log.error("External API error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        Map<String, String> body = new HashMap<>();
        body.put("error", "Book database service unavailable. Please try again later.");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    /** Catches unexpected errors and returns a safe 500 response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        Map<String, String> body = new HashMap<>();
        body.put("error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
