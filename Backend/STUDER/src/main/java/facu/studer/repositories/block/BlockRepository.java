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

    @Query("SELECT COUNT(b) FROM Block b WHERE b.parentBlock.id = :blockId AND b.isActive = true")
    long countForksByParentId(@Param("blockId") Long blockId);

    @Query("SELECT COUNT(bv) FROM BlockVersion bv WHERE bv.block.id = :blockId AND bv.isActive = true")
    long countVersionsByBlockId(@Param("blockId") Long blockId);
}
