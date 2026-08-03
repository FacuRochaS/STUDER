package facu.studer.repositories.block;

import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.discussions.DiscussionMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlockVersionRepository extends JpaRepository<BlockVersion, Long> {
    BlockVersion findFirstById(Long id);


    @Query("SELECT bv FROM BlockVersion bv " +
            "WHERE bv.block.id = :blockId " +
            "AND bv.isActive = true " +
            "AND bv.block.isActive = true ")
    List<BlockVersion> findBlockVersionsByBlockId(
            @Param("blockId") Long blockId);
}

