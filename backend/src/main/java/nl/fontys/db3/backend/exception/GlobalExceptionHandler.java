package nl.fontys.db3.backend.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ERROR = "error";
    private static final String MESSAGE_INVALID_CREDENTIALS = "Invalid credentials";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Resource not found";
        log.warn("Resource not found - message: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.NOT_FOUND.value(),
                KEY_ERROR, errorMessage
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed - errors: {}", errors);
        return ResponseEntity.badRequest().body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.BAD_REQUEST.value(),
                KEY_ERROR, errors
        ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Resource not found";
        log.warn("Entity not found - message: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.NOT_FOUND.value(),
                KEY_ERROR, errorMessage
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Unique-constraint violations (duplicate username/email) should be 409, not 500
        String errorMessage = "A resource with the same unique field already exists";
        log.warn("Data integrity violation - message: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.CONFLICT.value(),
                KEY_ERROR, errorMessage
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        log.warn("Bad request - message: {}", errorMessage);
        return ResponseEntity.badRequest().body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.BAD_REQUEST.value(),
                KEY_ERROR, errorMessage
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Internal server error";
        log.error("Illegal state exception - message: {}", errorMessage, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value(),
                KEY_ERROR, errorMessage
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed - message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.UNAUTHORIZED.value(),
                KEY_ERROR, MESSAGE_INVALID_CREDENTIALS
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials - message: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.UNAUTHORIZED.value(),
                KEY_ERROR, MESSAGE_INVALID_CREDENTIALS
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        log.error("Unexpected exception - message: {}, type: {}", errorMessage, ex.getClass().getSimpleName(), ex);
        return ResponseEntity.internalServerError().body(Map.of(
                KEY_TIMESTAMP, LocalDateTime.now(),
                KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value(),
                KEY_ERROR, errorMessage
        ));
    }
}
