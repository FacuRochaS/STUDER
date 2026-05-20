package facu.studer.services;

import facu.studer.entities.Tag;

import java.util.List;
import java.util.Set;

/**
 * Service interface for Tag operations.
 * Handles finding or creating tags by name.
 */
public interface TagService {

    /**
     * Finds existing tags by name, creating any that don't exist.
     *
     * @param names list of tag names
     * @return set of Tag entities (existing or newly created)
     */
    Set<Tag> findOrCreateByNames(List<String> names);
}

