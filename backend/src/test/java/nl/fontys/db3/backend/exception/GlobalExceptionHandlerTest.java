package nl.fontys.db3.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleNotFound - should return 404 with correct structure")
    void handleNotFound_Returns404() {
        NotFoundException ex = new NotFoundException("Resource not found");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
        assertEquals("Resource not found", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("handleIllegalArgument - should return 404 for 'not found' message")
    void handleIllegalArgument_NotFoundMessage_Returns404() {
        IllegalArgumentException ex = new IllegalArgumentException("User not found");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
        assertEquals("User not found", response.getBody().get("error"));
    }

    @Test
    @DisplayName("handleIllegalArgument - should return 404 for message containing 'not found'")
    void handleIllegalArgument_ContainsNotFound_Returns404() {
        IllegalArgumentException ex = new IllegalArgumentException("Entity not found in database");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
    }

    @Test
    @DisplayName("handleIllegalArgument - should return 400 for other messages")
    void handleIllegalArgument_OtherMessage_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertEquals("Invalid input", response.getBody().get("error"));
    }

    @Test
    @DisplayName("handleIllegalArgument - should return 400 for null message")
    void handleIllegalArgument_NullMessage_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertEquals("Bad request", response.getBody().get("error")); // null message gets default message
    }

    @Test
    @DisplayName("handleIllegalState - should return 500 with correct structure")
    void handleIllegalState_Returns500() {
        IllegalStateException ex = new IllegalStateException("Internal error occurred");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
        assertEquals("Internal error occurred", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("handleAuthentication - should return 401 with invalid credentials message")
    void handleAuthentication_Returns401() {
        AuthenticationException ex = new AuthenticationException("Authentication failed") {};
        ResponseEntity<Map<String, Object>> response = handler.handleAuthentication(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().get("status"));
        assertEquals("Invalid credentials", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("handleBadCredentials - should return 401 with invalid credentials message")
    void handleBadCredentials_Returns401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().get("status"));
        assertEquals("Invalid credentials", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("handleGeneric - should return 500 for generic exceptions")
    void handleGeneric_Returns500() {
        RuntimeException ex = new RuntimeException("Unexpected error");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
        assertEquals("Unexpected error", response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("handleGeneric - should handle null message")
    void handleGeneric_NullMessage_Returns500() {
        RuntimeException ex = new RuntimeException((String) null);
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
        assertEquals("An unexpected error occurred", response.getBody().get("error")); // null message gets default message
    }

}
