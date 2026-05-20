package facu.studer.repositories;

import facu.studer.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository for Tag entity.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Finds a tag by its name.
     */
    Optional<Tag> findByNameAndIsActiveTrue(String name);

    /**
     * Finds tags by names.
     */
    Set<Tag> findByNameInAndIsActiveTrue(Set<String> names);
}

