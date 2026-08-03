package facu.studer.services;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.courses.*;
import org.springframework.data.domain.Pageable;

public interface CourseService {
    CourseResponseDTO create(String username, CourseCreateRequestDTO request);
    CourseResponseDTO getById(String username, Long courseId);
    CoursePageResponseDTO getCourses(String username, int page, String filter, String tag);
    CoursePageResponseDTO searchCourses(String username, String query, Pageable pageable);
    CoursePageResponseDTO getUserCourses(String username, int page);
    CoursePageResponseDTO getCoursesByUsername(String requestingUser, String targetUsername, int page);
    CoursePageResponseDTO getFavouriteCourses(String username, int page);
    CourseResponseDTO update(String username, Long courseId, CourseUpdateRequestDTO request);
    MessageDTO addFavourite(String username, Long courseId);
    MessageDTO removeFavourite(String username, Long courseId);
    UserCourseBlockResponseDTO saveUserCourseBlock(String username, UserCourseBlockRequestDTO request);
}
