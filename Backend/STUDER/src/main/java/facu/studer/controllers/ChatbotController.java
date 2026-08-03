package facu.studer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ia")
public class ChatbotController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String EASYAI_URL = "http://localhost:8001/api/v1/execute/";
    private static final String API_KEY = "studer-secret-key";

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody Map<String, String> body) {
        String input = body.get("input");
        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing input"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", API_KEY);

            Map<String, String> payload = Map.of(
                "template", "CHAT",
                "context_id", "STUDER_GENERAL",
                "input", input
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                EASYAI_URL, HttpMethod.POST, request, String.class
            );

            String body2 = response.getBody();
            if (body2 != null && !body2.isBlank()) {
                return ResponseEntity.ok(body2);
            }
            return ResponseEntity.ok(Map.of("output", "No response from AI").toString());
        } catch (Exception e) {
            log.error("EasyAI proxy failed: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                "error", "AI unavailable",
                "detail", e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "")
            ));
        }
    }
}
