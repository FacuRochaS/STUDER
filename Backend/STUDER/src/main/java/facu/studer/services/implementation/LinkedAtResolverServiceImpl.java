package facu.studer.services.implementation;

import facu.studer.entities.LinkedType;
import facu.studer.entities.User;
import facu.studer.entities.discussions.Discussion;
import facu.studer.models.LinkedAtModel;
import facu.studer.services.LinkedAtResolverService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of LinkedAtResolverService.
 * Uses EntityManager directly to avoid multiple repository dependencies.
 * Resolves different entity types based on LinkedType.
 */
@Service
public class LinkedAtResolverServiceImpl implements LinkedAtResolverService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Resolves linked entity information by type and ID.
     * Uses EntityManager.find() to look up entities generically.
     */
    @Override
    @Transactional(readOnly = true)
    public LinkedAtModel resolve(LinkedType type, Long linkedId) {
        if (type == null || linkedId == null) {
            return null;
        }

        return switch (type) {
            case USER -> resolveUser(linkedId);
            case DISCUSSION -> resolveDiscussion(linkedId);
            case MESSAGE -> resolveMessage(linkedId);
            case EVENT -> resolveEvent(linkedId);
            case SYSTEM -> resolveSystem(linkedId);
        };
    }

    private LinkedAtModel resolveUser(Long id) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            return null;
        }
        return LinkedAtModel.builder()
                .linkedId(id)
                .linkedType(LinkedType.USER)
                .title(user.getUsername())
                .content(user.getFirstName() + " " + user.getLastName())
                .dateTime(user.getCreatedDatetime())
                .active(user.getIsActive())
                .build();
    }

    private LinkedAtModel resolveDiscussion(Long id) {
        Discussion discussion = entityManager.find(Discussion.class, id);
        if (discussion == null) {
            return null;
        }
        return LinkedAtModel.builder()
                .linkedId(id)
                .linkedType(LinkedType.DISCUSSION)
                .title(discussion.getTitle())
                .content(discussion.getDescription())
                .dateTime(discussion.getCreatedDatetime())
                .active(discussion.getIsActive())
                .build();
    }

    private LinkedAtModel resolveMessage(Long id) {
        // Placeholder for direct message resolution
        return LinkedAtModel.builder()
                .linkedId(id)
                .linkedType(LinkedType.MESSAGE)
                .title("Message")
                .content("")
                .active(true)
                .build();
    }

    private LinkedAtModel resolveEvent(Long id) {
        // Placeholder for event resolution
        return LinkedAtModel.builder()
                .linkedId(id)
                .linkedType(LinkedType.EVENT)
                .title("Event")
                .content("")
                .active(true)
                .build();
    }

    private LinkedAtModel resolveSystem(Long id) {
        return LinkedAtModel.builder()
                .linkedId(id)
                .linkedType(LinkedType.SYSTEM)
                .title("System Notification")
                .content("")
                .active(true)
                .build();
    }
}

