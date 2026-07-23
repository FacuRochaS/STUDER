package facu.studer.controllers;

import facu.studer.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private final RestTemplate restTemplate;
    private final SecurityUtils securityUtils;
    private final String mediaServiceUrl;

    public UploadController(RestTemplate restTemplate,
                            SecurityUtils securityUtils,
                            @Value("${app.media.service.url}") String mediaServiceUrl) {
        this.restTemplate = restTemplate;
        this.securityUtils = securityUtils;
        this.mediaServiceUrl = mediaServiceUrl;
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        securityUtils.requireCurrentUsername();

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = mediaServiceUrl + "/upload-image/message";
            log.info("Forwarding image to media service at {}", url);

            @SuppressWarnings("unchecked")
            Map<String, Object> mediaResponse = restTemplate.postForObject(url, requestEntity, Map.class);

            if (mediaResponse != null && mediaResponse.containsKey("urls")) {
                @SuppressWarnings("unchecked")
                Map<String, String> urls = (Map<String, String>) mediaResponse.get("urls");
                String originalUrl = urls.get("original");

                return ResponseEntity.ok(Map.of(
                        "url", originalUrl,
                        "filename", file.getOriginalFilename(),
                        "size", file.getSize(),
                        "mimeType", file.getContentType()
                ));
            }

            log.warn("Media service returned an unexpected response: {}", mediaResponse);
            return ResponseEntity.status(502).body(Map.of("error", "Media service returned an unexpected response"));
        } catch (Exception e) {
            log.error("Failed to upload image to media service", e);
            return ResponseEntity.status(502).body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}
