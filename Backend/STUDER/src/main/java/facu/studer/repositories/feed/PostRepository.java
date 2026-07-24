package facu.studer.repositories.feed;

import facu.studer.entities.feed.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.isActive = true ORDER BY p.createdDatetime DESC")
    Page<Post> findRecentPosts(Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN PostLike pl ON pl.post = p AND pl.isActive = true WHERE p.isActive = true GROUP BY p ORDER BY COUNT(pl) DESC, p.createdDatetime DESC")
    Page<Post> findPopularPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.user.username IN :usernames ORDER BY p.createdDatetime DESC")
    Page<Post> findByUserUsernames(@Param("usernames") List<String> usernames, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.user.id IN :userIds ORDER BY p.createdDatetime DESC")
    Page<Post> findByUserIds(@Param("userIds") List<Long> userIds, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isActive = true AND p.user.id = :userId ORDER BY p.createdDatetime DESC")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
