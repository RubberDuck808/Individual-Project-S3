package nl.fontys.db3.backend.service.storage;

import nl.fontys.db3.backend.config.StorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class StorageUrlService {

    private final StorageProperties props;

    public StorageUrlService(StorageProperties props) {
        this.props = props;
    }

    /** If DB stores full object path e.g. "icons/hazards/report-accident.svg" */
    public String publicUrlFromPath(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) return null;

        // split to ensure proper encoding of each segment
        return UriComponentsBuilder
                .fromUriString(props.getPublicBaseUrl())
                .pathSegment(objectPath.split("/"))
                .build()
                .toUriString();
    }

    /** If DB stores just filename e.g. "report-accident.svg" */
    public String hazardIconUrlFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        String prefix = props.getHazardIconsPrefix();
        return UriComponentsBuilder
                .fromUriString(props.getPublicBaseUrl())
                .pathSegment(prefix.split("/"))
                .pathSegment(fileName)
                .build()
                .toUriString();
    }

    /** Later: preset avatars from filename */
    public String presetAvatarUrlFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        String prefix = props.getPresetAvatarsPrefix();
        return UriComponentsBuilder
                .fromUriString(props.getPublicBaseUrl())
                .pathSegment(prefix.split("/"))
                .pathSegment(fileName)
                .build()
                .toUriString();
    }

    public String presetBackgroundUrlFromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        String prefix = props.getPresetBackgroundsPrefix();
        return UriComponentsBuilder
                .fromUriString(props.getPublicBaseUrl())
                .pathSegment(prefix.split("/"))
                .pathSegment(fileName)
                .build()
                .toUriString();
    }
}
