package facu.studer.services;

import facu.studer.entities.LinkedType;
import facu.studer.models.LinkedAtModel;

/**
 * Service for resolving linked entity information.
 * Given a LinkedType and an ID, returns a generic LinkedAtModel with the entity info.
 */
public interface LinkedAtResolverService {

    /**
     * Resolves linked entity information by type and ID.
     *
     * @param type     the linked entity type
     * @param linkedId the linked entity ID
     * @return the resolved LinkedAtModel, or null if not found
     */
    LinkedAtModel resolve(LinkedType type, Long linkedId);
}

