package facu.studer.repositories.block;

import facu.studer.entities.blocks.BlockLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockLikeRepository extends JpaRepository<BlockLike, Long> {
    boolean existsByUserUsernameAndBlockIdAndIsActiveTrue(String username, Long blockId);
    Optional<BlockLike> findByUserUsernameAndBlockIdAndIsActiveTrue(String username, Long blockId);
    long countByBlockIdAndIsActiveTrue(Long blockId);
}
