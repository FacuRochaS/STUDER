package facu.studer.services.implementation;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.courses.*;
import facu.studer.entities.Tag;
import facu.studer.entities.blocks.Block;
import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.courses.Course;
import facu.studer.entities.courses.CourseBlock;
import facu.studer.entities.courses.UserCourseBlock;
import facu.studer.entities.courses.UserCourseFav;
import facu.studer.entities.users.User;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.CourseMapper;
import facu.studer.repositories.courses.CourseBlockRepository;
import facu.studer.repositories.courses.CourseRepository;
import facu.studer.repositories.courses.UserCourseBlockRepository;
import facu.studer.repositories.courses.UserCourseFavRepository;
import facu.studer.repositories.block.BlockRepository;
import facu.studer.repositories.block.BlockVersionRepository;
import facu.studer.services.CourseService;
import facu.studer.services.PointsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private static final int PAGE_SIZE = 15;

    private final CourseRepository courseRepository;
    private final CourseBlockRepository courseBlockRepository;
    private final UserCourseFavRepository userCourseFavRepository;
    private final UserCourseBlockRepository userCourseBlockRepository;
    private final BlockRepository blockRepository;
    private final BlockVersionRepository blockVersionRepository;
    private final PointsService pointsService;

    @PersistenceContext
    private EntityManager entityManager;

    public CourseServiceImpl(CourseRepository courseRepository,
                             CourseBlockRepository courseBlockRepository,
                             UserCourseFavRepository userCourseFavRepository,
                             UserCourseBlockRepository userCourseBlockRepository,
                             BlockRepository blockRepository,
                             BlockVersionRepository blockVersionRepository,
                             PointsService pointsService) {
        this.courseRepository = courseRepository;
        this.courseBlockRepository = courseBlockRepository;
        this.userCourseFavRepository = userCourseFavRepository;
        this.userCourseBlockRepository = userCourseBlockRepository;
        this.blockRepository = blockRepository;
        this.blockVersionRepository = blockVersionRepository;
        this.pointsService = pointsService;
    }

    @Override
    @Transactional
    public CourseResponseDTO create(String username, CourseCreateRequestDTO request) {
        User owner = findUserByUsername(username);

        if (courseRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new IllegalArgumentException("course.slug_exists");
        }

        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        Course course = Course.builder()
                .owner(owner)
                .name(request.getName())
                .slug(request.getSlug())
                .tags(managedTags)
                .link(request.getLink() != null ? request.getLink() : "")
                .published(true)
                .contest(null)
                .contestHidden(false)
                .ratingSum(0L)
                .ratingCount(0)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Course savedCourse = courseRepository.save(course);

        if (request.getBlocks() != null) {
            saveCourseBlocks(savedCourse, request.getBlocks());
        }

        entityManager.flush();

        pointsService.addPoints(owner, 3L);

        return mapToDTO(savedCourse, username, true);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponseDTO getById(String username, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("course.not_found"));
        return mapToDTO(course, username, true);
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePageResponseDTO getCourses(String username, int page, String filter, String tag) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Course> coursePage;

        if ("popular".equalsIgnoreCase(filter)) {
            coursePage = courseRepository.findPopularCourses(pageable);
        } else {
            coursePage = courseRepository.findRecentCourses(pageable);
        }

        List<CourseResponseDTO> dtos = coursePage.getContent().stream()
                .map(c -> mapToDTO(c, username, false))
                .collect(Collectors.toList());

        return CoursePageResponseDTO.builder()
                .courses(dtos)
                .totalElements(coursePage.getTotalElements())
                .hasMore(coursePage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePageResponseDTO getUserCourses(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Course> coursePage = courseRepository.findByOwnerUsername(username, pageable);

        List<CourseResponseDTO> dtos = coursePage.getContent().stream()
                .map(c -> mapToDTO(c, username, false))
                .collect(Collectors.toList());

        return CoursePageResponseDTO.builder()
                .courses(dtos)
                .totalElements(coursePage.getTotalElements())
                .hasMore(coursePage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePageResponseDTO getFavouriteCourses(String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Course> coursePage = courseRepository.findFavouriteCourses(username, pageable);

        List<CourseResponseDTO> dtos = coursePage.getContent().stream()
                .map(c -> mapToDTO(c, username, false))
                .collect(Collectors.toList());

        return CoursePageResponseDTO.builder()
                .courses(dtos)
                .totalElements(coursePage.getTotalElements())
                .hasMore(coursePage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional
    public CourseResponseDTO update(String username, Long courseId, CourseUpdateRequestDTO request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("course.not_found"));

        if (!course.getOwner().getUsername().equals(username)) {
            throw new IllegalArgumentException("course.not_owner");
        }

        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getSlug() != null) {
            Optional<Course> existing = courseRepository.findBySlug(request.getSlug());
            if (existing.isPresent() && !existing.get().getId().equals(courseId)) {
                throw new IllegalArgumentException("course.slug_exists");
            }
            course.setSlug(request.getSlug());
        }
        if (request.getLink() != null) {
            course.setLink(request.getLink());
        }
        if (request.getTags() != null) {
            Set<Tag> managedTags = resolveTagsByName(request.getTags());
            course.setTags(managedTags);
        }
        course.setLastUpdatedDatetime(LocalDateTime.now());

        if (request.getBlocks() != null) {
            courseBlockRepository.deleteByCourseId(courseId);
            saveCourseBlocks(course, request.getBlocks());
        }

        courseRepository.save(course);
        entityManager.flush();

        return mapToDTO(course, username, true);
    }

    @Override
    @Transactional
    public MessageDTO addFavourite(String username, Long courseId) {
        if (userCourseFavRepository.existsByUserUsernameAndCourseIdAndIsActiveTrue(username, courseId)) {
            throw new IllegalArgumentException("course.already_favourite");
        }

        User user = findUserByUsername(username);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("course.not_found"));

        UserCourseFav fav = UserCourseFav.builder()
                .user(user)
                .course(course)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        userCourseFavRepository.save(fav);

        pointsService.addPoints(course.getOwner(), 5L);

        return MessageDTO.builder()
                .success(true)
                .message("course.favourite_added")
                .build();
    }

    @Override
    @Transactional
    public MessageDTO removeFavourite(String username, Long courseId) {
        var favOpt = userCourseFavRepository.findByUserUsernameAndCourseIdAndIsActiveTrue(username, courseId);

        if (favOpt.isEmpty()) {
            throw new IllegalArgumentException("course.not_favourite");
        }

        UserCourseFav fav = favOpt.get();
        User courseOwner = fav.getCourse().getOwner();

        fav.setIsActive(false);
        fav.setLastUpdatedDatetime(LocalDateTime.now());
        userCourseFavRepository.save(fav);

        pointsService.deductPoints(courseOwner, 5L);

        return MessageDTO.builder()
                .success(true)
                .message("course.favourite_removed")
                .build();
    }

    @Override
    @Transactional
    public UserCourseBlockResponseDTO saveUserCourseBlock(String username, UserCourseBlockRequestDTO request) {
        User user = findUserByUsername(username);
        CourseBlock courseBlock = entityManager.find(CourseBlock.class, request.getCourseBlockId());
        if (courseBlock == null || !courseBlock.getIsActive()) {
            throw new ResourceNotFoundException("course_block.not_found");
        }

        UserCourseBlock ucb = userCourseBlockRepository
                .findByUserUsernameAndCourseBlockIdAndIsActiveTrue(username, request.getCourseBlockId())
                .orElse(null);

        if (ucb == null) {
            ucb = UserCourseBlock.builder()
                    .user(user)
                    .courseBlock(courseBlock)
                    .completed(request.getCompleted() != null ? request.getCompleted() : false)
                    .duration(request.getDuration())
                    .attempts(request.getAttempts())
                    .isActive(true)
                    .createdDatetime(LocalDateTime.now())
                    .lastUpdatedDatetime(LocalDateTime.now())
                    .build();
        } else {
            if (request.getCompleted() != null) ucb.setCompleted(request.getCompleted());
            if (request.getDuration() != null) ucb.setDuration(request.getDuration());
            if (request.getAttempts() != null) ucb.setAttempts(request.getAttempts());
            ucb.setLastUpdatedDatetime(LocalDateTime.now());
        }

        ucb = userCourseBlockRepository.save(ucb);

        return UserCourseBlockResponseDTO.builder()
                .id(ucb.getId())
                .courseBlockId(ucb.getCourseBlock().getId())
                .completed(ucb.getCompleted())
                .duration(ucb.getDuration())
                .attempts(ucb.getAttempts())
                .build();
    }

    private CourseResponseDTO mapToDTO(Course course, String username, boolean includeBlocks) {
        boolean isFav = userCourseFavRepository.existsByUserUsernameAndCourseIdAndIsActiveTrue(username, course.getId());
        long favCount = userCourseFavRepository.countByCourseIdAndIsActiveTrue(course.getId());

        List<CourseBlock> blocks = includeBlocks
                ? courseBlockRepository.findByCourseIdOrderByBlockOrderAsc(course.getId())
                : null;

        return CourseMapper.toResponseDTO(course, isFav, favCount, blocks);
    }

    private void saveCourseBlocks(Course course, List<CourseBlockRequestDTO> blockRequests) {
        for (CourseBlockRequestDTO blockReq : blockRequests) {
            Block block = blockRepository.findById(blockReq.getBlockId())
                    .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

            BlockVersion version = null;
            if (blockReq.getVersionId() != null) {
                version = blockVersionRepository.findById(blockReq.getVersionId())
                        .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));
            }

            CourseBlock courseBlock = CourseBlock.builder()
                    .course(course)
                    .block(block)
                    .version(version)
                    .blockOrder(blockReq.getOrder())
                    .isActive(true)
                    .createdDatetime(LocalDateTime.now())
                    .lastUpdatedDatetime(LocalDateTime.now())
                    .build();

            courseBlockRepository.save(courseBlock);
        }
    }

    private User findUserByUsername(String username) {
        var query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username AND u.isActive = true", User.class);
        query.setParameter("username", username);
        var results = query.getResultList();
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("user.not_found");
        }
        return results.get(0);
    }

    private Set<Tag> resolveTagsByName(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalized = tagNames.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(n -> !n.isBlank())
                .collect(Collectors.toSet());

        if (normalized.isEmpty()) {
            return new HashSet<>();
        }

        List<Tag> existing = entityManager.createQuery(
                        "SELECT t FROM Tag t WHERE t.name IN :names AND t.isActive = true", Tag.class)
                .setParameter("names", normalized)
                .getResultList();

        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> result = new HashSet<>(existing);

        for (String name : normalized) {
            if (!existingNames.contains(name)) {
                Tag newTag = Tag.builder()
                        .name(name)
                        .isActive(true)
                        .createdDatetime(LocalDateTime.now())
                        .lastUpdatedDatetime(LocalDateTime.now())
                        .build();
                entityManager.persist(newTag);
                result.add(newTag);
            }
        }

        return result;
    }
}
