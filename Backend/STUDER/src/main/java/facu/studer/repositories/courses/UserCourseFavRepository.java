package facu.studer.repositories.courses;
import facu.studer.entities.courses.UserCourseFav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCourseFavRepository extends JpaRepository<UserCourseFav, Long> {
}
