package facu.studer.repositories.courses;

import facu.studer.entities.courses.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findBySlug(String slug);

    Integer countCoursesByName(String name);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND c.published = true ORDER BY c.createdDatetime DESC")
    Page<Course> findRecentCourses(Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.tags t WHERE c.isActive = true AND c.published = true AND t.name IN :tags ORDER BY c.createdDatetime DESC")
    Page<Course> findRecentCoursesByTags(@Param("tags") List<String> tags, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND c.published = true ORDER BY c.ratingCount DESC, c.createdDatetime DESC")
    Page<Course> findPopularCourses(Pageable pageable);

    @Query("SELECT c FROM Course c JOIN c.tags t WHERE c.isActive = true AND c.published = true AND t.name IN :tags ORDER BY c.ratingCount DESC, c.createdDatetime DESC")
    Page<Course> findPopularCoursesByTags(@Param("tags") List<String> tags, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND c.published = true AND c.owner.username = :username ORDER BY c.createdDatetime DESC")
    Page<Course> findByOwnerUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT c FROM Course c JOIN UserCourseFav f ON f.course = c WHERE c.isActive = true AND c.published = true AND f.user.username = :username AND f.isActive = true ORDER BY f.createdDatetime DESC")
    Page<Course> findFavouriteCourses(@Param("username") String username, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND c.contest.id = :contestId ORDER BY c.createdDatetime DESC")
    Page<Course> findByContestId(@Param("contestId") Long contestId, Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.isActive = true AND c.contest.id = :contestId")
    List<Course> findAllByContestId(@Param("contestId") Long contestId);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.isActive = true AND c.contest.id = :contestId")
    long countByContestId(@Param("contestId") Long contestId);
}
