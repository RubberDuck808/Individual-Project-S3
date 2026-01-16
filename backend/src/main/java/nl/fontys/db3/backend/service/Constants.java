package nl.fontys.db3.backend.service;

/**
 * Constants for error messages and string literals used across services.
 */
public final class Constants {
    
    private Constants() {
        // Utility class - prevent instantiation
    }
    
    // User-related error messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String FRIEND_REQUEST_NOT_FOUND = "Friend request not found";
    
    // Device-related error messages
    public static final String DEVICE_NOT_FOUND_PREFIX = "Device not found: ";
    
    // Asset-related error messages
    public static final String AVATAR_NOT_FOUND_PREFIX = "Avatar not found: ";
    public static final String BACKGROUND_NOT_FOUND_PREFIX = "Background not found: ";
    
    // Authentication error messages
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String CURRENT_PASSWORD_REQUIRED = "Current password is required";
    public static final String CURRENT_PASSWORD_INCORRECT = "Current password is incorrect";
    
    // Validation error messages
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String NAME_REQUIRED = "Name is required";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    
    // Time constants
    public static final int HOURS_IN_DAY = 24;
    public static final int DEVICE_ACTIVE_THRESHOLD_HOURS = 24;
    public static final int HAZARD_EXPIRATION_HOURS = 24;
    
    // Data limits
    public static final int MAX_TELEMETRY_HISTORY_LIMIT = 500;
    public static final int MIN_TELEMETRY_HISTORY_LIMIT = 1;
    
    // Diagnostic code patterns
    public static final String DIAGNOSTIC_CODE_PATTERN = "^[PBCU]\\d{4}$";
    public static final int DIAGNOSTIC_CODE_PREFIX_LENGTH = 1;
    
    // Role names
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    
    // Default numeric values
    public static final double DEFAULT_DISTANCE_KM = 0.0;
}
