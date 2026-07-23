package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.courses.*;
import facu.studer.security.SecurityUtils;
import facu.studer.services.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;
    private final SecurityUtils securityUtils;

    public CourseController(CourseService courseService, SecurityUtils securityUtils) {
        this.courseService = courseService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<CourseResponseDTO> createCourse(
            @Valid @RequestBody CourseCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        CourseResponseDTO response = courseService.create(username, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> getCourse(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        CourseResponseDTO response = courseService.getById(username, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CoursePageResponseDTO> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "recent") String filter,
            @RequestParam(required = false) List<String> tags) {
        String username = securityUtils.requireCurrentUsername();
        String tag = (tags != null && !tags.isEmpty()) ? tags.get(0) : null;
        CoursePageResponseDTO response = courseService.getCourses(username, page, filter, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CoursePageResponseDTO> getUserCourses(
            @RequestParam(defaultValue = "0") int page) {
        String username = securityUtils.requireCurrentUsername();
        CoursePageResponseDTO response = courseService.getUserCourses(username, page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favourites")
    public ResponseEntity<CoursePageResponseDTO> getFavouriteCourses(
            @RequestParam(defaultValue = "0") int page) {
        String username = securityUtils.requireCurrentUsername();
        CoursePageResponseDTO response = courseService.getFavouriteCourses(username, page);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        CourseResponseDTO response = courseService.update(username, id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/favourite")
    public ResponseEntity<MessageDTO> addFavourite(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = courseService.addFavourite(username, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/favourite")
    public ResponseEntity<MessageDTO> removeFavourite(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = courseService.removeFavourite(username, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/blocks/interaction")
    public ResponseEntity<UserCourseBlockResponseDTO> saveUserCourseBlock(
            @Valid @RequestBody UserCourseBlockRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        UserCourseBlockResponseDTO response = courseService.saveUserCourseBlock(username, request);
        return ResponseEntity.ok(response);
    }
}
