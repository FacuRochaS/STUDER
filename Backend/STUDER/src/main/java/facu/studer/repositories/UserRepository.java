package facu.studer.repositories;

import facu.studer.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User.
 * Provides CRUD operations and query methods for insurer contact information.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Checks if a user exists with the given email.
     * @param email the email to check
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     * @param username the username to check
     * @return true if a user exists with the username, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Finds a user by username.
     * @param username the username to search
     * @return the User entity, or null if not found
     */
    User findByUsername(String username);

    /**
     * Finds a user by email.
     * @param email the email to search
     * @return the User entity, or null if not found
     */
    User findByEmail(String email);

    /**
     * Searches users by username (case-insensitive).
     * @param username the username fragment
     * @param pageable pagination info
     * @return a page of users
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
