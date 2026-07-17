package facu.studer.repositories.courses;

import facu.studer.entities.courses.CourseBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseBlockRepository extends JpaRepository<CourseBlock, Long> {
    List<CourseBlock> findByCourseIdOrderByBlockOrderAsc(Long courseId);

    @Modifying
    @Query("DELETE FROM CourseBlock cb WHERE cb.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}
