package facu.studer.DTOs.media;

import lombok.Data;
import java.util.Map;

@Data
public class ImageUploadResponseDTO {
    private Map<String, String> urls;
}
