package facu.studer.controllers;

import facu.studer.DTOs.discussions.*;
import facu.studer.security.SecurityUtils;
import facu.studer.services.DiscussionMessageService;
import facu.studer.services.DiscussionService;
import facu.studer.services.MessageLikeService;
import facu.studer.services.UserDiscussionFavService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for public discussion operations.
 * All endpoints require authentication via JWT token.
 * Manages discussions, messages, favourites, and likes.
 */
@RestController
@RequestMapping("/api/v1/discussions")
public class DiscussionController {

    private final DiscussionService discussionService;
    private final DiscussionMessageService discussionMessageService;
    private final UserDiscussionFavService userDiscussionFavService;
    private final MessageLikeService messageLikeService;
    private final SecurityUtils securityUtils;

    public DiscussionController(
            DiscussionService discussionService,
            DiscussionMessageService discussionMessageService,
            UserDiscussionFavService userDiscussionFavService,
            MessageLikeService messageLikeService,
            SecurityUtils securityUtils) {
        this.discussionService = discussionService;
        this.discussionMessageService = discussionMessageService;
        this.userDiscussionFavService = userDiscussionFavService;
        this.messageLikeService = messageLikeService;
        this.securityUtils = securityUtils;
    }

    /**
     * Gets paginated public discussions with optional filters.
     * Ordered by recent activity (messages in the last N hours).
     *
     * @param page          page number (0-based, default 0)
     * @param tags          optional comma-separated tag names
     * @param lastDays      optional filter for discussions created in last N days
     * @param activityHours optional hours window for activity ordering (default 24)
     * @return paginated discussion response
     */
    @GetMapping
    public ResponseEntity<DiscussionPageResponseDTO> getPublicDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Integer lastDays,
            @RequestParam(required = false) Integer activityHours) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionPageResponseDTO response = discussionService
                .getPublicDiscussions(username, page, tags, lastDays, activityHours);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets paginated discussions where the authenticated user has participated
     * (as owner, messaged, or favourited).
     *
     * @param page page number (0-based, default 0)
     * @return paginated discussion response with participation info
     */
    @GetMapping("/me")
    public ResponseEntity<DiscussionPageResponseDTO> getUserDiscussions(
            @RequestParam(defaultValue = "0") int page) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionPageResponseDTO response = discussionService.getUserDiscussions(username, page);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a single discussion by its ID.
     *
     * @param id the discussion ID
     * @return the discussion response
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponseDTO> getDiscussionById(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        DiscussionResponseDTO response = discussionService.getById(username, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new public discussion.
     * Tags are resolved (created if they don't exist).
     *
     * @param request the discussion creation data
     * @return the created discussion response
     */
    @PostMapping
    public ResponseEntity<DiscussionResponseDTO> createDiscussion(
            @Valid @RequestBody DiscussionCreateRequestDTO request) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionResponseDTO response = discussionService.create(username, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets paginated messages for a discussion with hierarchy.
     * Root messages are paginated; children are nested under their parent.
     * Ordered by like count (most liked first).
     *
     * @param id   the discussion ID
     * @param page page number (0-based, default 0)
     * @return paginated message response with nested children
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<DiscussionMessagePageResponseDTO> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionMessagePageResponseDTO response = discussionMessageService
                .getMessages(id, username, page);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new message in a discussion (or replies to an existing message).
     * Fails if the discussion is closed.
     *
     * @param id      the discussion ID
     * @param request the message creation data
     * @return the created message response
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<DiscussionMessageResponseDTO> createMessage(
            @PathVariable Long id,
            @Valid @RequestBody DiscussionMessageCreateRequestDTO request) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionMessageResponseDTO response = discussionMessageService
                .createMessage(id, username, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a discussion to the authenticated user's favourites.
     *
     * @param id the discussion ID
     * @return success/error response
     */
    @PostMapping("/{id}/favourite")
    public ResponseEntity<MessageResponseDTO> addFavourite(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageResponseDTO response = userDiscussionFavService.addFavourite(username, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a discussion from the authenticated user's favourites.
     *
     * @param id the discussion ID
     * @return success/error response
     */
    @DeleteMapping("/{id}/favourite")
    public ResponseEntity<MessageResponseDTO> removeFavourite(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageResponseDTO response = userDiscussionFavService.removeFavourite(username, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a like to a discussion message.
     * Each user can only like a message once.
     *
     * @param messageId the message ID
     * @return success/error response
     */
    @PostMapping("/messages/{messageId}/like")
    public ResponseEntity<MessageResponseDTO> likeMessage(@PathVariable Long messageId) {
        String username = securityUtils.requireCurrentUsername();
        MessageResponseDTO response = messageLikeService.like(username, messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a like from a discussion message.
     *
     * @param messageId the message ID
     * @return success/error response
     */
    @DeleteMapping("/messages/{messageId}/like")
    public ResponseEntity<MessageResponseDTO> unlikeMessage(@PathVariable Long messageId) {
        String username = securityUtils.requireCurrentUsername();
        MessageResponseDTO response = messageLikeService.unlike(username, messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Closes a discussion. Only the owner can close it.
     * A closed discussion cannot receive new messages but remains visible.
     *
     * @param id the discussion ID
     * @return success/error response
     */
    @PatchMapping("/{id}/close")
    public ResponseEntity<MessageResponseDTO> closeDiscussion(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageResponseDTO response = discussionService.close(username, id);
        return ResponseEntity.ok(response);
    }
}

