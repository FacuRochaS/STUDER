package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.feed.PostCreateRequestDTO;
import facu.studer.DTOs.feed.PostPageResponseDTO;
import facu.studer.DTOs.feed.PostResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.FeedService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {

    private final FeedService feedService;
    private final SecurityUtils securityUtils;

    public FeedController(FeedService feedService, SecurityUtils securityUtils) {
        this.feedService = feedService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @Valid @RequestBody PostCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        PostResponseDTO response = feedService.createPost(username, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PostPageResponseDTO> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "recent") String filter) {
        String username = securityUtils.requireCurrentUsername();
        PostPageResponseDTO response = feedService.getFeed(username, page, filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PostPageResponseDTO> getUserPosts(
            @RequestParam(defaultValue = "0") int page,
            @PathVariable Long userId) {
        String username = securityUtils.requireCurrentUsername();
        PostPageResponseDTO response = feedService.getUserPosts(username, page, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<MessageDTO> likePost(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = feedService.likePost(username, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<MessageDTO> unlikePost(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = feedService.unlikePost(username, id);
        return ResponseEntity.ok(response);
    }
}
