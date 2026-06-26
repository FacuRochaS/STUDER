package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.chats.MessageRequestDTO;
import facu.studer.DTOs.chats.MessageResponseDTO;
import facu.studer.DTOs.discussions.*;
import facu.studer.security.SecurityUtils;

import facu.studer.services.DiscussionService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final SecurityUtils securityUtils;

    /**
     * Constructs a DiscussionController with the required core discussion domain service
     * and security operations utility helper.
     *
     * @param discussionService the service executing core discussion thread logic and filters
     * @param securityUtils       the security component for identity contextual lookups
     */
    public DiscussionController(
            DiscussionService discussionService,
            SecurityUtils securityUtils) {
        this.discussionService = discussionService;
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
     * (as owner, messaged, or favourite).
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
     * Gets paginated discussions where the authenticated user has marked as favourite
     *
     * @param page page number (0-based, default 0)
     * @return paginated discussion response with participation info
     */
    @GetMapping("/favourites")
    public ResponseEntity<DiscussionPageResponseDTO> getUserFavouriteDiscussions(
            @RequestParam(defaultValue = "0") int page) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionPageResponseDTO response = discussionService.getFavouriteDiscussions(username, page);
        return ResponseEntity.ok(response);
    }


    /**
     * Gets paginated discussions where the authenticated user is the owner
     *
     * @param page page number (0-based, default 0)
     * @return paginated discussion response with participation info
     */
    @GetMapping("/own")
    public ResponseEntity<DiscussionPageResponseDTO> getUserOwnDiscussions(
            @RequestParam(defaultValue = "0") int page) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionPageResponseDTO response = discussionService.getUserOwnDiscussions(username, page);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets paginated popular discussions
     *
     * @param page page number (0-based, default 0)
     * @return paginated discussion response with participation info
     */
    @GetMapping("/popular")
    public ResponseEntity<DiscussionPageResponseDTO> getPopularDiscussions(
            @RequestParam(defaultValue = "0") int page) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionPageResponseDTO response = discussionService.getPopularDiscussions(username, page);
        return ResponseEntity.ok(response);
    }


    /**
     * Gets paginated new discussions
     *
     * @param page page number (0-based, default 0)
     * @return paginated discussion response with participation info
     */
    @GetMapping("/new")
    public ResponseEntity<DiscussionPageResponseDTO> getNewDiscussions(
            @RequestParam(defaultValue = "0") int page) {

        String username = securityUtils.requireCurrentUsername();
        DiscussionPageResponseDTO response = discussionService.getNewDiscussions(username, page);
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
        DiscussionMessagePageResponseDTO response = discussionService
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
    @PostMapping(value="/{id}/messages", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<DiscussionMessageResponseDTO> createMessage(
            @PathVariable Long id,
            @Valid @RequestPart("request") DiscussionMessageCreateRequestDTO request,
             @RequestPart(value = "file", required = false) MultipartFile file){

        String username = securityUtils.requireCurrentUsername();
        DiscussionMessageResponseDTO response = discussionService
                .createMessage(id, username, request, file);
        return ResponseEntity.ok(response);
    }


    /**
     * Adds a discussion to the authenticated user's favourites.
     *
     * @param id the discussion ID
     * @return success/error response
     */
    @PostMapping("/{id}/favourite")
    public ResponseEntity<MessageDTO> addFavourite(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = discussionService.addFavourite(username, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a discussion from the authenticated user's favourites.
     *
     * @param id the discussion ID
     * @return success/error response
     */
    @DeleteMapping("/{id}/favourite")
    public ResponseEntity<MessageDTO> removeFavourite(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = discussionService.removeFavourite(username, id);
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
    public ResponseEntity<MessageDTO> likeMessage(@PathVariable Long messageId) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = discussionService.like(username, messageId);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a like from a discussion message.
     *
     * @param messageId the message ID
     * @return success/error response
     */
    @DeleteMapping("/messages/{messageId}/like")
    public ResponseEntity<MessageDTO> unlikeMessage(@PathVariable Long messageId) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = discussionService.unlike(username, messageId);
        return ResponseEntity.ok(response);
    }

}

