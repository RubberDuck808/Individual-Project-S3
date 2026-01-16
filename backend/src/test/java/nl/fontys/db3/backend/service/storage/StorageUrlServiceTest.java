package nl.fontys.db3.backend.service.storage;

import nl.fontys.db3.backend.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StorageUrlService Tests")
class StorageUrlServiceTest {

    private StorageUrlService service;
    private StorageProperties props;

    @BeforeEach
    void setUp() {
        props = new StorageProperties();
        props.setPublicBaseUrl("https://storage.example.com");
        props.setHazardIconsPrefix("icons/hazards");
        props.setPresetAvatarsPrefix("avatars/preset");
        props.setPresetBackgroundsPrefix("backgrounds/preset");
        service = new StorageUrlService(props);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("publicUrlFromPath - should return null for null/blank input")
    void publicUrlFromPath_NullOrBlankInput_ReturnsNull(String input) {
        String result = service.publicUrlFromPath(input);
        assertNull(result);
    }

    @Test
    @DisplayName("publicUrlFromPath - should return full URL for valid path")
    void publicUrlFromPath_ValidPath_ReturnsFullUrl() {
        String result = service.publicUrlFromPath("icons/hazards/report-accident.svg");
        assertNotNull(result);
        assertEquals("https://storage.example.com/icons/hazards/report-accident.svg", result);
    }

    @Test
    @DisplayName("publicUrlFromPath - should handle path with multiple segments")
    void publicUrlFromPath_MultipleSegments_ReturnsFullUrl() {
        String result = service.publicUrlFromPath("avatars/user/profile.jpg");
        assertNotNull(result);
        assertEquals("https://storage.example.com/avatars/user/profile.jpg", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("hazardIconUrlFromFileName - should return null for null/blank input")
    void hazardIconUrlFromFileName_NullOrBlankInput_ReturnsNull(String input) {
        String result = service.hazardIconUrlFromFileName(input);
        assertNull(result);
    }

    @Test
    @DisplayName("hazardIconUrlFromFileName - should return full URL for valid filename")
    void hazardIconUrlFromFileName_ValidFileName_ReturnsFullUrl() {
        String result = service.hazardIconUrlFromFileName("report-accident.svg");
        assertNotNull(result);
        assertEquals("https://storage.example.com/icons/hazards/report-accident.svg", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("presetAvatarUrlFromFileName - should return null for null/blank input")
    void presetAvatarUrlFromFileName_NullOrBlankInput_ReturnsNull(String input) {
        String result = service.presetAvatarUrlFromFileName(input);
        assertNull(result);
    }

    @Test
    @DisplayName("presetAvatarUrlFromFileName - should return full URL for valid filename")
    void presetAvatarUrlFromFileName_ValidFileName_ReturnsFullUrl() {
        String result = service.presetAvatarUrlFromFileName("avatar1.png");
        assertNotNull(result);
        assertEquals("https://storage.example.com/avatars/preset/avatar1.png", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("presetBackgroundUrlFromFileName - should return null for null/blank input")
    void presetBackgroundUrlFromFileName_NullOrBlankInput_ReturnsNull(String input) {
        String result = service.presetBackgroundUrlFromFileName(input);
        assertNull(result);
    }

    @Test
    @DisplayName("presetBackgroundUrlFromFileName - should return full URL for valid filename")
    void presetBackgroundUrlFromFileName_ValidFileName_ReturnsFullUrl() {
        String result = service.presetBackgroundUrlFromFileName("bg1.jpg");
        assertNotNull(result);
        assertEquals("https://storage.example.com/backgrounds/preset/bg1.jpg", result);
    }
}
