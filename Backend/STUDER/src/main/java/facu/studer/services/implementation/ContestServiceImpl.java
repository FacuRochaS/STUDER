package facu.studer.services.implementation;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.blocks.BlockVersionResponseDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.DTOs.courses.*;
import facu.studer.entities.Tag;
import facu.studer.entities.blocks.Block;
import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.contest.Contest;
import facu.studer.entities.contest.CourseRating;
import facu.studer.entities.courses.Course;
import facu.studer.entities.courses.CourseBlock;
import facu.studer.entities.users.User;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.CourseMapper;
import facu.studer.repositories.block.BlockRepository;
import facu.studer.repositories.block.BlockVersionRepository;
import facu.studer.repositories.contest.ContestRepository;
import facu.studer.repositories.contest.CourseRatingRepository;
import facu.studer.repositories.courses.CourseBlockRepository;
import facu.studer.repositories.courses.CourseRepository;
import facu.studer.services.ContestService;
import facu.studer.services.PointsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContestServiceImpl implements ContestService {

    private final ContestRepository contestRepository;
    private final CourseRepository courseRepository;
    private final CourseBlockRepository courseBlockRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final BlockRepository blockRepository;
    private final BlockVersionRepository blockVersionRepository;
    private final PointsService pointsService;

    @PersistenceContext
    private EntityManager entityManager;

    public ContestServiceImpl(ContestRepository contestRepository,
                              CourseRepository courseRepository,
                              CourseBlockRepository courseBlockRepository,
                              CourseRatingRepository courseRatingRepository,
                              BlockRepository blockRepository,
                              BlockVersionRepository blockVersionRepository,
                              PointsService pointsService) {
        this.contestRepository = contestRepository;
        this.courseRepository = courseRepository;
        this.courseBlockRepository = courseBlockRepository;
        this.courseRatingRepository = courseRatingRepository;
        this.blockRepository = blockRepository;
        this.blockVersionRepository = blockVersionRepository;
        this.pointsService = pointsService;
    }

    @Override
    @Transactional
    public ContestResponseDTO createContest(String username, ContestCreateRequestDTO request) {
        User admin = findUserByUsername(username);
        if (!"ADMIN".equals(admin.getRole())) {
            throw new IllegalArgumentException("contest.unauthorized");
        }

        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .tags(managedTags)
                .startDate(request.getStartDate())
                .changeDate(request.getStartDate().plusDays(7))
                .endDate(request.getStartDate().plusDays(14))
                .status("PREPARATION")
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Contest saved = contestRepository.save(contest);
        entityManager.flush();

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContestResponseDTO getContest(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));
        return mapToDTO(contest);
    }

    @Override
    @Transactional
    public CourseResponseDTO submitCourseForContest(String username, Long contestId, CourseCreateRequestDTO request) {
        User owner = findUserByUsername(username);

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        if (!"PREPARATION".equals(contest.getStatus())) {
            throw new IllegalArgumentException("contest.not_in_preparation");
        }

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
                .published(false)
                .contest(contest)
                .contestHidden(true)
                .ratingSum(0L)
                .ratingCount(0)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Course savedCourse = courseRepository.save(course);

        if (request.getBlocks() != null) {
            for (CourseBlockRequestDTO blockReq : request.getBlocks()) {
                Block block = blockRepository.findById(blockReq.getBlockId())
                        .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

                BlockVersion version = null;
                if (blockReq.getVersionId() != null) {
                    version = blockVersionRepository.findById(blockReq.getVersionId())
                            .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));
                }

                CourseBlock courseBlock = CourseBlock.builder()
                        .course(savedCourse)
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

        entityManager.flush();

        boolean isFav = false;
        long favCount = 0;
        return CourseMapper.toResponseDTO(savedCourse, isFav, favCount,
                courseBlockRepository.findByCourseIdOrderByBlockOrderAsc(savedCourse.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ContestCourseResponseDTO getRandomCourseForValidation(String username, Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        if (!"VALIDATION".equals(contest.getStatus())) {
            throw new IllegalArgumentException("contest.not_in_validation");
        }

        User user = findUserByUsername(username);
        LocalDateTime now = LocalDateTime.now();
        long daysUntilEnd = contest.getEndDate().toLocalDate().toEpochDay() - now.toLocalDate().toEpochDay();

        List<Course> eligibleCourses;

        if (daysUntilEnd <= 3 && daysUntilEnd >= 0) {
            eligibleCourses = courseRepository.findAllByContestId(contestId).stream()
                    .filter(c -> c.getRatingCount() > 0)
                    .filter(c -> !courseRatingRepository.existsByUserUsernameAndCourseIdAndIsActiveTrue(username, c.getId()))
                    .filter(c -> {
                        if (c.getRatingCount() > 0) {
                            double avg = (double) c.getRatingSum() / c.getRatingCount();
                            return avg >= 1.5;
                        }
                        return true;
                    })
                    .sorted((a, b) -> {
                        double avgA = a.getRatingCount() > 0 ? (double) a.getRatingSum() / a.getRatingCount() : 0;
                        double avgB = b.getRatingCount() > 0 ? (double) b.getRatingSum() / b.getRatingCount() : 0;
                        return Double.compare(avgB, avgA);
                    })
                    .collect(Collectors.toList());
        } else {
            eligibleCourses = courseRepository.findAllByContestId(contestId).stream()
                    .filter(c -> !courseRatingRepository.existsByUserUsernameAndCourseIdAndIsActiveTrue(username, c.getId()))
                    .filter(c -> {
                        if (c.getRatingCount() > 0) {
                            double avg = (double) c.getRatingSum() / c.getRatingCount();
                            return avg >= 1.5;
                        }
                        return true;
                    })
                    .sorted((a, b) -> {
                        boolean aUnrated = a.getRatingCount() == 0;
                        boolean bUnrated = b.getRatingCount() == 0;
                        if (aUnrated && !bUnrated) return -1;
                        if (!aUnrated && bUnrated) return 1;
                        return 0;
                    })
                    .collect(Collectors.toList());
        }

        if (eligibleCourses.isEmpty()) {
            throw new ResourceNotFoundException("contest.no_courses_available");
        }

        Collections.shuffle(eligibleCourses);
        Course selected = eligibleCourses.get(0);

        List<CourseBlock> blocks = courseBlockRepository.findByCourseIdOrderByBlockOrderAsc(selected.getId());
        List<CourseBlockResponseDTO> blockDTOs = blocks.stream().map(cb -> {
            BlockVersion version = cb.getVersion() != null ? cb.getVersion() : cb.getBlock().getCurrentVersion();
            BlockVersionResponseDTO versionDTO = BlockVersionResponseDTO.builder()
                    .id(version.getId())
                    .createdDatetime(version.getCreatedDatetime())
                    .lastUpdatedDatetime(version.getLastUpdatedDatetime())
                    .content(String.valueOf(version.getContent()))
                    .versionNumber(version.getVersionNumber())
                    .build();
            return CourseBlockResponseDTO.builder()
                    .id(cb.getId())
                    .blockId(cb.getBlock().getId())
                    .blockName(cb.getBlock().getName())
                    .version(versionDTO)
                    .order(cb.getBlockOrder())
                    .build();
        }).collect(Collectors.toList());

        double avgRating = selected.getRatingCount() > 0
                ? (double) selected.getRatingSum() / selected.getRatingCount()
                : 0.0;

        return ContestCourseResponseDTO.builder()
                .id(selected.getId())
                .name(selected.getName())
                .slug(selected.getSlug())
                .tags(selected.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .link(selected.getLink())
                .ratingSum(selected.getRatingSum())
                .ratingCount(selected.getRatingCount())
                .averageRating(Math.round(avgRating * 100.0) / 100.0)
                .createdDatetime(selected.getCreatedDatetime())
                .blocks(blockDTOs)
                .build();
    }

    @Override
    @Transactional
    public MessageDTO rateCourse(String username, CourseRatingRequestDTO request) {
        User user = findUserByUsername(username);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("course.not_found"));

        if (course.getContest() == null || !"VALIDATION".equals(course.getContest().getStatus())) {
            throw new IllegalArgumentException("contest.not_in_validation");
        }

        if (courseRatingRepository.existsByUserUsernameAndCourseIdAndIsActiveTrue(username, request.getCourseId())) {
            throw new IllegalArgumentException("contest.already_rated");
        }

        CourseRating rating = CourseRating.builder()
                .user(user)
                .course(course)
                .rating(request.getRating())
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        courseRatingRepository.save(rating);

        course.setRatingSum(course.getRatingSum() + request.getRating());
        course.setRatingCount(course.getRatingCount() + 1);
        courseRepository.save(course);

        pointsService.addPoints(course.getOwner(), 5L);

        return MessageDTO.builder()
                .success(true)
                .message("contest.rating_added")
                .build();
    }

    @Override
    @Transactional
    public MessageDTO finishContest(String username, Long contestId) {
        User admin = findUserByUsername(username);
        if (!"ADMIN".equals(admin.getRole())) {
            throw new IllegalArgumentException("contest.unauthorized");
        }

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        List<Course> contestCourses = courseRepository.findAllByContestId(contestId);

        for (Course course : contestCourses) {
            course.setPublished(true);
            course.setContestHidden(false);
            course.setContest(null);
            course.setLastUpdatedDatetime(LocalDateTime.now());
            courseRepository.save(course);
        }

        contest.setStatus("FINISHED");
        contest.setLastUpdatedDatetime(LocalDateTime.now());
        contestRepository.save(contest);

        return MessageDTO.builder()
                .success(true)
                .message("contest.finished")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getLeaderboard(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        List<Course> courses = courseRepository.findAllByContestId(contestId);

        return courses.stream()
                .filter(c -> c.getRatingCount() > 0)
                .map(c -> {
                    double avgRating = (double) c.getRatingSum() / c.getRatingCount();
                    long likeCount = 0;
                    double score = (avgRating * 0.7) + (Math.min(likeCount, 100) * 0.003);
                    return LeaderboardEntryDTO.builder()
                            .courseId(c.getId())
                            .courseName(c.getName())
                            .averageRating(Math.round(avgRating * 100.0) / 100.0)
                            .ratingCount((long) c.getRatingCount())
                            .likeCount(likeCount)
                            .score(Math.round(score * 100.0) / 100.0)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    private ContestResponseDTO mapToDTO(Contest contest) {
        List<String> tagNames = contest.getTags() != null
                ? contest.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : List.of();

        return ContestResponseDTO.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .content(contest.getContent())
                .tags(tagNames)
                .status(contest.getStatus())
                .startDate(contest.getStartDate())
                .changeDate(contest.getChangeDate())
                .endDate(contest.getEndDate())
                .createdDatetime(contest.getCreatedDatetime())
                .build();
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
