package facu.studer.services;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.courses.*;

public interface CourseService {
    CourseResponseDTO create(String username, CourseCreateRequestDTO request);
    CourseResponseDTO getById(String username, Long courseId);
    CoursePageResponseDTO getCourses(String username, int page, String filter, String tag);
    CoursePageResponseDTO getUserCourses(String username, int page);
    CoursePageResponseDTO getFavouriteCourses(String username, int page);
    CourseResponseDTO update(String username, Long courseId, CourseUpdateRequestDTO request);
    MessageDTO addFavourite(String username, Long courseId);
    MessageDTO removeFavourite(String username, Long courseId);
    UserCourseBlockResponseDTO saveUserCourseBlock(String username, UserCourseBlockRequestDTO request);
}
