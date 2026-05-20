package facu.studer.services;

import facu.studer.DTOs.user.*;
import facu.studer.entities.User;

/**
 * Service interface for User operations.
 * All operations that modify user data require the current authenticated username
 * to validate ownership.
 */
public interface UserService {

    /**
     * Creates a new user (registration).
     *
     * @param request creation data
     * @return created user response
     */
    UserResponseDTO create(UserCreateRequestDTO request);

    /**
     * Updates a user. Only allows updating the user's OWN data.
     *
     * @param currentUsername the authenticated username
     * @param request update data
     * @return updated user response
     */
    UserResponseDTO update(String currentUsername, UserUpdateRequestDTO request);

    /**
     * Soft deletes a user. Only allows deleting the user's OWN account.
     *
     * @param currentUsername the authenticated username
     * @return deleted user response
     */
    UserResponseDTO softDelete(String currentUsername);

    /**
     * Gets a user by ID. Validates the current user has access.
     *
     * @param id the user ID to retrieve
     * @param currentUsername the authenticated username
     * @return user response
     */
    UserResponseDTO getById(Long id, String currentUsername);

    /**
     * Finds a user entity by username.
     *
     * @param username the username
     * @return the User entity, or null if not found
     */
    User findByUsername(String username);

    /**
     * Gets a user response by username.
     *
     * @param username the username
     * @return UserResponseDTO
     */
    UserResponseDTO getByUsername(String username);
}
