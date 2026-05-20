package facu.studer.services.implementation;

import facu.studer.DTOs.user.*;
import facu.studer.entities.User;
import facu.studer.exceptions.UnauthorizedOperationException;
import facu.studer.mappers.UserMapper;
import facu.studer.repositories.UserRepository;
import facu.studer.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementation for managing Users.
 * All operations validate ownership - users can only modify their OWN data.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user with hashed password (registration).
     */
    @Override
    @Transactional
    public UserResponseDTO create(UserCreateRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("user.email.unique");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("user.username.unique");
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedDatetime(LocalDateTime.now());
        user.setLastUpdatedDatetime(LocalDateTime.now());
        user.setIsActive(true);

        User saved = userRepository.save(user);
        return UserMapper.toResponseDTO(saved);
    }

    /**
     * Updates a user. Validates that the user can only update their OWN account.
     */
    @Override
    @Transactional
    public UserResponseDTO update(String currentUsername, UserUpdateRequestDTO request) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new IllegalArgumentException("user.not_found");
        }

        // Update fields
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if email is already taken by another user
            User existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser != null && !existingUser.getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException("user.email.unique");
            }
            currentUser.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        currentUser.setLastUpdatedDatetime(LocalDateTime.now());
        User updated = userRepository.save(currentUser);
        return UserMapper.toResponseDTO(updated);
    }

    /**
     * Soft deletes a user. Users can only delete their OWN account.
     */
    @Override
    @Transactional
    public UserResponseDTO softDelete(String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new IllegalArgumentException("user.not_found");
        }

        currentUser.setIsActive(false);
        currentUser.setLastUpdatedDatetime(LocalDateTime.now());
        User deleted = userRepository.save(currentUser);
        return UserMapper.toResponseDTO(deleted);
    }

    /**
     * Gets a user by ID. Validates the current user has access (ownership).
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getById(Long id, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new IllegalArgumentException("user.not_found");
        }

        // Users can only view their own data
        if (!currentUser.getId().equals(id)) {
            throw new UnauthorizedOperationException("auth.unauthorized_operation");
        }

        return UserMapper.toResponseDTO(currentUser);
    }

    /**
     * Gets a user response by username.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("user.not_found");
        }
        return UserMapper.toResponseDTO(user);
    }

    /**
     * Finds a user entity by username.
     */
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
