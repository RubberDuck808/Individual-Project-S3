package nl.fontys.db3.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage();
        if (message != null && (message.contains("not found") || message.contains("User not found"))) {
            log.warn("Illegal argument treated as not found - message: {}", message);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    KEY_TIMESTAMP, LocalDateTime.now(),
                    KEY_STATUS, HttpStatus.NOT_FOUND.value(),
                    KEY_ERROR, message
            ));
        }
        String errorMessage = message != null ? message : "Bad request";
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
