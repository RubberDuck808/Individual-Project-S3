package nl.fontys.db3.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String publicBaseUrl;
    private String hazardIconsPrefix;
    private String presetAvatarsPrefix;
    private String presetBackgroundsPrefix;

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getHazardIconsPrefix() {
        return hazardIconsPrefix;
    }

    public void setHazardIconsPrefix(String hazardIconsPrefix) {
        this.hazardIconsPrefix = hazardIconsPrefix;
    }

    public String getPresetAvatarsPrefix() {
        return presetAvatarsPrefix;
    }

    public void setPresetAvatarsPrefix(String presetAvatarsPrefix) {
        this.presetAvatarsPrefix = presetAvatarsPrefix;
    }

    public String getPresetBackgroundsPrefix() {
        return presetBackgroundsPrefix;
    }

    public void setPresetBackgroundsPrefix(String presetBackgroundsPrefix) {
        this.presetBackgroundsPrefix = presetBackgroundsPrefix;
    }

    
}
