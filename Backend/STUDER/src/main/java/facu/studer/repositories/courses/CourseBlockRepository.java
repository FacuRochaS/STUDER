package facu.studer.repositories.courses;
import facu.studer.entities.courses.CourseBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseBlockRepository extends JpaRepository<CourseBlock, Long> {
}
