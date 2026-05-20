package facu.studer.services.implementation;

import facu.studer.DTOs.discussions.DiscussionCreateRequestDTO;
import facu.studer.DTOs.discussions.DiscussionPageResponseDTO;
import facu.studer.DTOs.discussions.DiscussionResponseDTO;
import facu.studer.DTOs.discussions.MessageResponseDTO;
import facu.studer.entities.Tag;
import facu.studer.entities.User;
import facu.studer.entities.discussions.Discussion;
import facu.studer.exceptions.DiscussionClosedException;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.exceptions.UnauthorizedOperationException;
import facu.studer.mappers.DiscussionMapper;
import facu.studer.repositories.DiscussionRepository;
import facu.studer.services.DiscussionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of DiscussionService.
 * Uses only DiscussionRepository (respects 1-repo-per-service).
 * Uses EntityManager for User lookups and favourite checks.
 */
@Service
public class DiscussionServiceImpl implements DiscussionService {

    private static final int PAGE_SIZE = 10;
    private static final int DEFAULT_ACTIVITY_HOURS = 24;

    private final DiscussionRepository discussionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public DiscussionServiceImpl(DiscussionRepository discussionRepository) {
        this.discussionRepository = discussionRepository;
    }

    /**
     * Gets paginated public discussions with filters, ordered by recent activity.
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionPageResponseDTO getPublicDiscussions(
            String username,
            int page,
            List<String> tagNames,
            Integer lastDays,
            Integer activityHours) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        LocalDateTime since = lastDays != null ? LocalDateTime.now().minusDays(lastDays) : null;
        int hours = activityHours != null ? activityHours : DEFAULT_ACTIVITY_HOURS;
        LocalDateTime activitySince = LocalDateTime.now().minusHours(hours);
        boolean hasTags = tagNames != null && !tagNames.isEmpty();

        Page<Discussion> discussionPage = discussionRepository.findPublicDiscussionsByActivity(
                hasTags,
                hasTags ? tagNames : List.of(),
                since,
                activitySince,
                pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Gets discussions where the user participates (owner or messaged) and favourited.
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionPageResponseDTO getUserDiscussions(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<Discussion> discussionPage = discussionRepository
                .findByUserParticipation(username, pageable);

        List<DiscussionResponseDTO> dtos = discussionPage.getContent().stream()
                .map(d -> mapToDTO(d, username))
                .collect(Collectors.toList());

        return DiscussionPageResponseDTO.builder()
                .discussions(dtos)
                .totalElements(discussionPage.getTotalElements())
                .hasMore(discussionPage.hasNext())
                .currentPage(page)
                .build();
    }

    /**
     * Gets a single discussion by ID.
     */
    @Override
    @Transactional(readOnly = true)
    public DiscussionResponseDTO getById(String username, Long discussionId) {
        Discussion discussion = discussionRepository.findByIdAndIsActiveTrue(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("discussion.not_found"));
        return mapToDTO(discussion, username);
    }

    /**
     * Creates a new public discussion with the first message embedded in description.
     * Tags are resolved internally within this transaction.
     */
    @Override
    @Transactional
    public DiscussionResponseDTO create(String username, DiscussionCreateRequestDTO request) {
        User owner = findUserByUsername(username);

        // Resolve tags within the same persistence context
        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        Discussion discussion = Discussion.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .owner(owner)
                .tags(managedTags)
                .closed(false)
                .messageCount(0)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Discussion saved = discussionRepository.save(discussion);
        entityManager.flush();
        return mapToDTO(saved, username);
    }

    /**
     * Closes a discussion. Only the owner can close it.
     * A closed discussion cannot receive new messages.
     */
    @Override
    @Transactional
    public MessageResponseDTO close(String username, Long discussionId) {
        Discussion discussion = discussionRepository.findByIdAndIsActiveTrue(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("discussion.not_found"));

        if (!discussion.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedOperationException("discussion.unauthorized_close");
        }

        if (discussion.isClosed()) {
            throw new DiscussionClosedException("discussion.already_closed");
        }

        discussion.setClosed(true);
        discussion.setClosedAt(LocalDateTime.now());
        discussion.setLastUpdatedDatetime(LocalDateTime.now());
        discussionRepository.save(discussion);

        return MessageResponseDTO.builder()
                .success(true)
                .message("discussion.closed_success")
                .build();
    }

    /**
     * Maps a Discussion entity to DTO, determining user participation type.
     */
    private DiscussionResponseDTO mapToDTO(Discussion discussion, String username) {
        boolean isFav = isFavourite(username, discussion.getId());
        String participationType = determineParticipationType(discussion, username, isFav);
        return DiscussionMapper.toResponseDTO(discussion, isFav, participationType);
    }

    /**
     * Determines how the user participates in the discussion.
     */
    private String determineParticipationType(Discussion discussion, String username, boolean isFav) {
        if (discussion.getOwner().getUsername().equals(username)) {
            return "OWNER";
        }
        // Check if user has sent messages in this discussion
        Long messageCount = (Long) entityManager.createQuery(
                        "SELECT COUNT(dm) FROM DiscussionMessage dm " +
                                "WHERE dm.discussion.id = :discussionId AND dm.sender.username = :username AND dm.isActive = true")
                .setParameter("discussionId", discussion.getId())
                .setParameter("username", username)
                .getSingleResult();
        if (messageCount > 0) {
            return "MESSAGED";
        }
        if (isFav) {
            return "FAVOURITE";
        }
        return "NONE";
    }

    /**
     * Checks if user has favourited a discussion via EntityManager.
     */
    private boolean isFavourite(String username, Long discussionId) {
        Long count = (Long) entityManager.createQuery(
                        "SELECT COUNT(f) FROM UserDiscussionFav f " +
                                "WHERE f.user.username = :username AND f.discussion.id = :discussionId AND f.isActive = true")
                .setParameter("username", username)
                .setParameter("discussionId", discussionId)
                .getSingleResult();
        return count > 0;
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

    /**
     * Resolves tag names to managed Tag entities within the current persistence context.
     * Creates any tags that don't exist yet.
     */
    private Set<Tag> resolveTagsByName(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalized = tagNames.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(n -> !n.isBlank())
                .collect(Collectors.toSet());

        if (normalized.isEmpty()) {
            return new HashSet<>();
        }

        // Find existing tags in the current persistence context
        List<Tag> existing = entityManager.createQuery(
                        "SELECT t FROM Tag t WHERE t.name IN :names AND t.isActive = true", Tag.class)
                .setParameter("names", normalized)
                .getResultList();

        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> result = new HashSet<>(existing);

        // Create missing tags
        for (String name : normalized) {
            if (!existingNames.contains(name)) {
                Tag newTag = Tag.builder()
                        .name(name)
                        .isActive(true)
                        .createdDatetime(LocalDateTime.now())
                        .lastUpdatedDatetime(LocalDateTime.now())
                        .build();
                entityManager.persist(newTag);
                result.add(newTag);
            }
        }

        return result;
    }
}

