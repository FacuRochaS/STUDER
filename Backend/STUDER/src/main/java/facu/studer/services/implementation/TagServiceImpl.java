package facu.studer.services.implementation;

import facu.studer.entities.Tag;
import facu.studer.repositories.TagRepository;
import facu.studer.services.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of TagService.
 * Uses only TagRepository (respects 1-repo-per-service).
 */
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Finds existing tags by name. Creates any that don't exist yet.
     */
    @Override
    @Transactional
    public Set<Tag> findOrCreateByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalizedNames = names.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .filter(n -> !n.isBlank())
                .collect(Collectors.toSet());

        Set<Tag> existingTags = tagRepository.findByNameInAndIsActiveTrue(normalizedNames);

        Set<String> existingNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> newTags = normalizedNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(name -> {
                    Tag tag = Tag.builder()
                            .name(name)
                            .isActive(true)
                            .createdDatetime(LocalDateTime.now())
                            .lastUpdatedDatetime(LocalDateTime.now())
                            .build();
                    return tagRepository.save(tag);
                })
                .collect(Collectors.toSet());

        Set<Tag> allTags = new HashSet<>(existingTags);
        allTags.addAll(newTags);
        return allTags;
    }
}

