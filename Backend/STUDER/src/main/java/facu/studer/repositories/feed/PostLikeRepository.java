package facu.studer.repositories.feed;

import facu.studer.entities.feed.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUserUsernameAndPostIdAndIsActiveTrue(String username, Long postId);
    Optional<PostLike> findByUserUsernameAndPostIdAndIsActiveTrue(String username, Long postId);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.isActive = true")
    long countByPostId(@Param("postId") Long postId);

    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END FROM PostLike pl WHERE pl.post.id = :postId AND pl.user.username = :username AND pl.isActive = true")
    boolean isLikedByUser(@Param("postId") Long postId, @Param("username") String username);
}
