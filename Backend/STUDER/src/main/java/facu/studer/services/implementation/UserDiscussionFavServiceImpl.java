package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.entities.User;
import facu.studer.entities.discussions.Discussion;
import facu.studer.entities.discussions.UserDiscussionFav;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.repositories.UserDiscussionFavRepository;
import facu.studer.services.UserDiscussionFavService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of UserDiscussionFavService.
 * Uses only UserDiscussionFavRepository (respects 1-repo-per-service).
 * Uses EntityManager for lookups of User and Discussion.
 */
@Service
public class UserDiscussionFavServiceImpl implements UserDiscussionFavService {

    private final UserDiscussionFavRepository userDiscussionFavRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UserDiscussionFavServiceImpl(UserDiscussionFavRepository userDiscussionFavRepository) {
        this.userDiscussionFavRepository = userDiscussionFavRepository;
    }

    /**
     * Adds a discussion to the user's favourites.
     */
    @Override
    @Transactional
    public MessageResponseDTO addFavourite(String username, Long discussionId) {
        if (userDiscussionFavRepository.existsByUserUsernameAndDiscussionIdAndIsActiveTrue(username, discussionId)) {
            throw new IllegalArgumentException("discussion.already_favourite");
        }

        User user = findUserByUsername(username);
        Discussion discussion = entityManager.find(Discussion.class, discussionId);
        if (discussion == null || !discussion.getIsActive()) {
            throw new ResourceNotFoundException("discussion.not_found");
        }

        UserDiscussionFav fav = UserDiscussionFav.builder()
                .user(user)
                .discussion(discussion)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        userDiscussionFavRepository.save(fav);

        return MessageResponseDTO.builder()
                .success(true)
                .message("discussion.favourite_added")
                .build();
    }

    /**
     * Removes a discussion from the user's favourites (soft-delete).
     */
    @Override
    @Transactional
    public MessageResponseDTO removeFavourite(String username, Long discussionId) {
        Optional<UserDiscussionFav> favOpt = userDiscussionFavRepository
                .findByUserUsernameAndDiscussionIdAndIsActiveTrue(username, discussionId);

        if (favOpt.isEmpty()) {
            throw new IllegalArgumentException("discussion.not_favourite");
        }

        UserDiscussionFav fav = favOpt.get();
        fav.setIsActive(false);
        fav.setLastUpdatedDatetime(LocalDateTime.now());
        userDiscussionFavRepository.save(fav);

        return MessageResponseDTO.builder()
                .success(true)
                .message("discussion.favourite_removed")
                .build();
    }

    /**
     * Checks if a user has favourited a discussion.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFavourite(String username, Long discussionId) {
        return userDiscussionFavRepository
                .existsByUserUsernameAndDiscussionIdAndIsActiveTrue(username, discussionId);
    }

    private User findUserByUsername(String username) {
        var query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username AND u.isActive = true", User.class);
        query.setParameter("username", username);
        var results = query.getResultList();
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("user.not_found");
        }
        return results.get(0);
    }
}

