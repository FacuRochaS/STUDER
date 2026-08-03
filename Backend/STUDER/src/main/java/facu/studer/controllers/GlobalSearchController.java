package facu.studer.controllers;

import facu.studer.DTOs.blocks.BlockResponseDTO;
import facu.studer.DTOs.contest.ContestResponseDTO;
import facu.studer.DTOs.courses.CourseResponseDTO;
import facu.studer.DTOs.search.GlobalSearchResponseDTO;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import facu.studer.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class GlobalSearchController {

    private final SecurityUtils securityUtils;
    private final facu.studer.services.UserService userService;
    private final facu.studer.services.BlockService blockService;
    private final facu.studer.services.CourseService courseService;
    private final facu.studer.services.ContestService contestService;

    public GlobalSearchController(SecurityUtils securityUtils,
                                   facu.studer.services.UserService userService,
                                   facu.studer.services.BlockService blockService,
                                   facu.studer.services.CourseService courseService,
                                   facu.studer.services.ContestService contestService) {
        this.securityUtils = securityUtils;
        this.userService = userService;
        this.blockService = blockService;
        this.courseService = courseService;
        this.contestService = contestService;
    }

    @GetMapping
    public ResponseEntity<GlobalSearchResponseDTO> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int size) {

        String username;
        try { username = securityUtils.requireCurrentUsername(); } catch (Exception e) { username = "anonymous"; }
        final String uname = username;
        final String q = query;

        List<UserPublicResponseDTO> users = safe(() -> userService.searchByUsername(q, 0, size).getUsers());
        List<BlockResponseDTO> blocks = safe(() -> blockService.getBlocksBySearch(uname, 0, null, null, null, null, q).getBlocks());
        List<CourseResponseDTO> courses = safe(() -> courseService.searchCourses(uname, q, PageRequest.of(0, size)).getCourses());
        List<ContestResponseDTO> contests = safe(() -> contestService.searchContests(q, PageRequest.of(0, size)).getContent());

        return ResponseEntity.ok(GlobalSearchResponseDTO.builder()
                .users(users).blocks(blocks).courses(courses).contests(contests).build());
    }

    private static <T> List<T> safe(java.util.function.Supplier<List<T>> supplier) {
        try { return supplier.get(); } catch (Exception e) { return new ArrayList<>(); }
    }
}
