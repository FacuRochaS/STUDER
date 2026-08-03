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
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostCreateRequestDTO request) {
        return ResponseEntity.ok(feedService.createPost(securityUtils.requireCurrentUsername(), request));
    }

    @GetMapping("/yours")
    public ResponseEntity<PostPageResponseDTO> getYourPosts(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(feedService.getYourPosts(securityUtils.requireCurrentUsername(), page));
    }

    @GetMapping("/following")
    public ResponseEntity<PostPageResponseDTO> getFollowingPosts(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(feedService.getFollowingPosts(securityUtils.requireCurrentUsername(), page));
    }

    @GetMapping("/popular")
    public ResponseEntity<PostPageResponseDTO> getPopularPosts(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(feedService.getPopularPosts(securityUtils.requireCurrentUsername(), page));
    }

    @GetMapping("/new")
    public ResponseEntity<PostPageResponseDTO> getNewPosts(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(feedService.getNewPosts(securityUtils.requireCurrentUsername(), page));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PostPageResponseDTO> getUserPosts(@RequestParam(defaultValue = "0") int page, @PathVariable Long userId) {
        return ResponseEntity.ok(feedService.getUserPosts(securityUtils.requireCurrentUsername(), page, userId));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<MessageDTO> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(feedService.likePost(securityUtils.requireCurrentUsername(), id));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<MessageDTO> unlikePost(@PathVariable Long id) {
        return ResponseEntity.ok(feedService.unlikePost(securityUtils.requireCurrentUsername(), id));
    }
}
