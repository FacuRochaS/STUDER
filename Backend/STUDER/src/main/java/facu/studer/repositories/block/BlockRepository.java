package facu.studer.repositories.block;

import facu.studer.entities.blocks.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    Block findFirstById(Long id);

    Optional<Block> findBySlug(String slug);

    @Query("SELECT b FROM Block b " +
            "WHERE b.owner.id = :userId " +
            "AND b.isActive=true ")
    Page<Block> findByOwnerId(
            @Param("userId") Long userId,
            Pageable pageable);

    List<Block> findByParentBlock_Id(Long parentBlockId);

    Integer countBlocksByName(String name);
}
