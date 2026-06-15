package facu.studer.controllers;

import facu.studer.DTOs.user.UserCreateRequestDTO;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import facu.studer.DTOs.user.UserResponseDTO;
import facu.studer.DTOs.user.UserSearchPageResponseDTO;
import facu.studer.DTOs.user.UserUpdateRequestDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing Users.
 * CRUD operations only - authentication is handled by AuthController.
 *
 * Security: Users can only modify/delete their OWN account.
 * The authenticated user is determined from the JWT token.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    public UserController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    /**
     * Registers a new user (public endpoint).
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserCreateRequestDTO request) {
        return ResponseEntity.ok(userService.create(request));
    }

    /**
     * Updates the current authenticated user.
     * The user can only update their OWN account.
     */
    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<UserResponseDTO> update(@Valid @RequestPart("request") UserUpdateRequestDTO request,
                                                  @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(userService.update(securityUtils.requireCurrentUsername(), request, file));
    }

    /**
     * Soft deletes the current authenticated user.
     * The user can only delete their OWN account.
     */
    @DeleteMapping
    public ResponseEntity<UserResponseDTO> delete() {
        return ResponseEntity.ok(userService.softDelete(securityUtils.requireCurrentUsername()));
    }

    /**
     * Gets the current authenticated user's data.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getByUsername(securityUtils.requireCurrentUsername()));
    }

    /**
     * Gets a public user profile by username.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserPublicResponseDTO> getPublicByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getPublicByUsername(username));
    }

    /**
     * Searches users by username.
     */
    @GetMapping("/search")
    public ResponseEntity<UserSearchPageResponseDTO> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.searchByUsername(query, page, size));
    }

    /**
     * Gets a user by ID.
     * Users can only view their own data.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id, securityUtils.requireCurrentUsername()));
    }
}