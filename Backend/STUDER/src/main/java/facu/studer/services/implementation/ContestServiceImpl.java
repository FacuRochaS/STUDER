package facu.studer.services.implementation;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.blocks.BlockVersionResponseDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.DTOs.courses.*;
import facu.studer.entities.Tag;
import facu.studer.entities.blocks.Block;
import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.contest.Achievement;
import facu.studer.entities.contest.Contest;
import facu.studer.entities.contest.CourseRating;
import facu.studer.entities.contest.UserAchievement;
import facu.studer.entities.courses.Course;
import facu.studer.entities.courses.CourseBlock;
import facu.studer.entities.users.User;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.CourseMapper;
import facu.studer.repositories.block.BlockRepository;
import facu.studer.repositories.block.BlockVersionRepository;
import facu.studer.repositories.contest.*;
import facu.studer.repositories.courses.CourseBlockRepository;
import facu.studer.repositories.courses.CourseRepository;
import facu.studer.services.ContestService;
import facu.studer.services.PointsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContestServiceImpl implements ContestService {

    private static final List<String> VALID_STATUSES = List.of("ANNOUNCED", "PREPARATION", "BUILDING", "VALIDATION", "RESULTS", "CANCELLED");

    private final ContestRepository contestRepository;
    private final CourseRepository courseRepository;
    private final CourseBlockRepository courseBlockRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final BlockRepository blockRepository;
    private final BlockVersionRepository blockVersionRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final PointsService pointsService;

    @PersistenceContext
    private EntityManager entityManager;

    public ContestServiceImpl(ContestRepository contestRepository,
                              CourseRepository courseRepository,
                              CourseBlockRepository courseBlockRepository,
                              CourseRatingRepository courseRatingRepository,
                              BlockRepository blockRepository,
                              BlockVersionRepository blockVersionRepository,
                              AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository,
                              PointsService pointsService) {
        this.contestRepository = contestRepository;
        this.courseRepository = courseRepository;
        this.courseBlockRepository = courseBlockRepository;
        this.courseRatingRepository = courseRatingRepository;
        this.blockRepository = blockRepository;
        this.blockVersionRepository = blockVersionRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.pointsService = pointsService;
    }

    @PostConstruct
    @Transactional
    public void seedDefaultAchievements() {
        if (achievementRepository.count() > 0) return;
        Achievement[] defaults = {
            Achievement.builder().id(1L).name("contest_participant").description("Participated in a contest")
                .badgeUrl("/badges/participant.png").category("participation").criteria(null).build(),
            Achievement.builder().id(2L).name("contest_winner_gold").description("Won 1st place in a contest")
                .badgeUrl("/badges/gold.png").category("winner").criteria(null).build(),
            Achievement.builder().id(3L).name("contest_winner_silver").description("Won 2nd place in a contest")
                .badgeUrl("/badges/silver.png").category("winner").criteria(null).build(),
            Achievement.builder().id(4L).name("contest_winner_bronze").description("Won 3rd place in a contest")
                .badgeUrl("/badges/bronze.png").category("winner").criteria(null).build(),
        };
        for (Achievement a : defaults) {
            achievementRepository.save(a);
        }
    }

    // ===== ADMIN: CONTEST CRUD =====

    @Override
    @Transactional
    public ContestResponseDTO createContest(String username, ContestCreateRequestDTO request) {
        User admin = requireAdmin(username);

        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .banner(request.getBanner())
                .description(request.getDescription())
                .theme(request.getTheme())
                .difficulty(request.getDifficulty())
                .content(request.getContent())
                .tags(managedTags)
                .startDate(request.getStartDate())
                .preparationEndDate(request.getPreparationEndDate())
                .buildingEndDate(request.getBuildingEndDate())
                .validationEndDate(request.getValidationEndDate())
                .endDate(request.getEndDate())
                .status("ANNOUNCED")
                .rewards(request.getRewards())
                .externalLinks(request.getExternalLinks())
                .bibliography(request.getBibliography())
                .learningObjectives(request.getLearningObjectives())
                .minLevel(request.getMinLevel())
                .minReputation(request.getMinReputation())
                .maxParticipants(request.getMaxParticipants())
                .createdBy(admin)
                .participantCount(0)
                .courseCount(0)
                .blockCount(0)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Contest saved = contestRepository.save(contest);
        entityManager.flush();
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ContestResponseDTO updateContest(String username, Long contestId, ContestCreateRequestDTO request) {
        requireAdmin(username);
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        if (List.of("RESULTS", "CANCELLED").contains(contest.getStatus())) {
            throw new IllegalArgumentException("contest.cannot_update_finished");
        }

        Set<Tag> managedTags = request.getTags() != null ? resolveTagsByName(request.getTags()) : contest.getTags();

        contest.setTitle(request.getTitle());
        contest.setBanner(request.getBanner());
        contest.setDescription(request.getDescription());
        contest.setTheme(request.getTheme());
        contest.setDifficulty(request.getDifficulty());
        contest.setContent(request.getContent());
        contest.setTags(managedTags);
        contest.setStartDate(request.getStartDate());
        contest.setPreparationEndDate(request.getPreparationEndDate());
        contest.setBuildingEndDate(request.getBuildingEndDate());
        contest.setValidationEndDate(request.getValidationEndDate());
        contest.setEndDate(request.getEndDate());
        contest.setRewards(request.getRewards());
        contest.setExternalLinks(request.getExternalLinks());
        contest.setBibliography(request.getBibliography());
        contest.setLearningObjectives(request.getLearningObjectives());
        contest.setMinLevel(request.getMinLevel());
        contest.setMinReputation(request.getMinReputation());
        contest.setMaxParticipants(request.getMaxParticipants());
        contest.setLastUpdatedDatetime(LocalDateTime.now());

        Contest saved = contestRepository.save(contest);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public MessageDTO deleteContest(String username, Long contestId) {
        requireAdmin(username);
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));
        contest.setIsActive(false);
        contest.setLastUpdatedDatetime(LocalDateTime.now());
        contestRepository.save(contest);
        return MessageDTO.builder().success(true).message("contest.deleted").build();
    }

    @Override
    @Transactional
    public MessageDTO changeContestStatus(String username, Long contestId, String newStatus) {
        requireAdmin(username);
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("contest.invalid_status");
        }

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        List<String> validTransitions = getValidTransitions(contest.getStatus());
        if (!validTransitions.contains(newStatus)) {
            throw new IllegalArgumentException("contest.invalid_transition");
        }

        String oldStatus = contest.getStatus();
        contest.setStatus(newStatus);
        contest.setLastUpdatedDatetime(LocalDateTime.now());
        contestRepository.save(contest);

        // Handle phase entry logic
        if ("BUILDING".equals(newStatus) && !"BUILDING".equals(oldStatus)) {
            // Preparation just ended - courses move to hidden
            List<Course> courses = courseRepository.findAllByContestId(contestId);
            contest.setCourseCount(courses.size());
        }

        return MessageDTO.builder().success(true).message("contest.status_changed").build();
    }

    @Override
    @Transactional
    public MessageDTO finishContest(String username, Long contestId) {
        User admin = requireAdmin(username);
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        if (!"VALIDATION".equals(contest.getStatus())) {
            throw new IllegalArgumentException("contest.not_in_validation");
        }

        List<Course> contestCourses = courseRepository.findAllByContestId(contestId);
        for (Course course : contestCourses) {
            course.setPublished(true);
            course.setContestHidden(false);
            course.setLastUpdatedDatetime(LocalDateTime.now());
            courseRepository.save(course);
        }

        contest.setStatus("RESULTS");
        contest.setLastUpdatedDatetime(LocalDateTime.now());
        contestRepository.save(contest);

        // Award achievements
        awardParticipationAchievements(contest, contestCourses);

        return MessageDTO.builder().success(true).message("contest.finished").build();
    }

    // ===== PUBLIC: CONTEST LISTING =====

    @Override
    @Transactional(readOnly = true)
    public ContestResponseDTO getContest(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));
        return mapToDTO(contest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContestResponseDTO> listContests(String status, Pageable pageable) {
        Page<Contest> page;
        if (status != null && !status.isBlank()) {
            page = contestRepository.findByStatusOrderByStartDateDesc(status, pageable);
        } else {
            page = contestRepository.findAllByOrderByStartDateDesc(pageable);
        }
        return page.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContestResponseDTO> getActiveContests() {
        return contestRepository.findActiveContests(LocalDateTime.now())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContestResponseDTO> getUpcomingContests() {
        return contestRepository.findUpcomingContests(LocalDateTime.now())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // ===== PARTICIPATION =====

    @Override
    @Transactional
    public CourseResponseDTO submitCourseForContest(String username, Long contestId, CourseCreateRequestDTO request) {
        User owner = findUserByUsername(username);
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        if (!"BUILDING".equals(contest.getStatus())) {
            throw new IllegalArgumentException("contest.not_in_building");
        }

        if (contest.getMinLevel() != null && getLevel(owner.getPoints()) < contest.getMinLevel()) {
            throw new IllegalArgumentException("contest.level_too_low");
        }
        if (contest.getMinReputation() != null && owner.getPoints() < contest.getMinReputation()) {
            throw new IllegalArgumentException("contest.reputation_too_low");
        }
        if (contest.getMaxParticipants() != null && contest.getParticipantCount() >= contest.getMaxParticipants()) {
            throw new IllegalArgumentException("contest.max_participants_reached");
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
                BlockVersion version = blockReq.getVersionId() != null
                        ? blockVersionRepository.findById(blockReq.getVersionId())
                                .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"))
                        : null;
                CourseBlock courseBlock = CourseBlock.builder()
                        .course(savedCourse).block(block).version(version)
                        .blockOrder(blockReq.getOrder()).isActive(true)
                        .createdDatetime(LocalDateTime.now()).lastUpdatedDatetime(LocalDateTime.now())
                        .build();
                courseBlockRepository.save(courseBlock);
            }
        }

        contest.setParticipantCount(contest.getParticipantCount() + 1);
        contest.setCourseCount((int) courseRepository.countByContestId(contestId));
        contest.setLastUpdatedDatetime(LocalDateTime.now());
        contestRepository.save(contest);

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

        List<Course> eligible = courseRepository.findAllByContestId(contestId).stream()
                .filter(c -> !courseRatingRepository.existsByUserUsernameAndCourseIdAndIsActiveTrue(username, c.getId()))
                .filter(c -> c.getRatingCount() == 0 || (double) c.getRatingSum() / c.getRatingCount() >= 1.5)
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            throw new ResourceNotFoundException("contest.no_courses_available");
        }

        Collections.shuffle(eligible);
        Course selected = eligible.get(0);
        return buildContestCourseDTO(selected);
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
                .user(user).course(course)
                .rating(request.getRating()).isActive(true)
                .createdDatetime(LocalDateTime.now()).lastUpdatedDatetime(LocalDateTime.now())
                .build();
        courseRatingRepository.save(rating);

        course.setRatingSum(course.getRatingSum() + request.getRating());
        course.setRatingCount(course.getRatingCount() + 1);
        courseRepository.save(course);

        pointsService.addPoints(course.getOwner(), 5L);
        pointsService.addPoints(user, 10L);

        return MessageDTO.builder().success(true).message("contest.rating_added").build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getLeaderboard(Long contestId) {
        List<Course> courses = courseRepository.findAllByContestId(contestId);
        return courses.stream()
                .filter(c -> c.getRatingCount() > 0)
                .map(c -> {
                    double avg = (double) c.getRatingSum() / c.getRatingCount();
                    double score = (avg * 0.7) + (Math.min(c.getRatingCount(), 100) * 0.003);
                    return LeaderboardEntryDTO.builder()
                            .courseId(c.getId()).courseName(c.getName())
                            .averageRating(Math.round(avg * 100.0) / 100.0)
                            .ratingCount((long) c.getRatingCount())
                            .likeCount(0L)
                            .score(Math.round(score * 100.0) / 100.0)
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    // ===== ACHIEVEMENTS =====

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponseDTO> getUserAchievements(String username) {
        List<UserAchievement> unlocked = userAchievementRepository.findByUserUsernameAndIsActiveTrue(username);
        Set<Long> unlockedIds = unlocked.stream().map(ua -> ua.getAchievement().getId()).collect(Collectors.toSet());
        Map<Long, LocalDateTime> unlockedAt = unlocked.stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua.getCreatedDatetime()));

        return achievementRepository.findAll().stream()
                .map(a -> AchievementResponseDTO.builder()
                        .id(a.getId()).name(a.getName()).description(a.getDescription())
                        .badgeUrl(a.getBadgeUrl()).category(a.getCategory()).criteria(a.getCriteria())
                        .unlocked(unlockedIds.contains(a.getId()))
                        .unlockedAt(unlockedAt.get(a.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponseDTO> getAllAchievements() {
        return achievementRepository.findAll().stream()
                .map(a -> AchievementResponseDTO.builder()
                        .id(a.getId()).name(a.getName()).description(a.getDescription())
                        .badgeUrl(a.getBadgeUrl()).category(a.getCategory()).criteria(a.getCriteria())
                        .unlocked(false).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void awardAchievement(String username, Long achievementId) {
        if (userAchievementRepository.existsByUserUsernameAndAchievementIdAndIsActiveTrue(username, achievementId)) return;
        User user = findUserByUsername(username);
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("achievement.not_found"));
        UserAchievement ua = UserAchievement.builder()
                .user(user).achievement(achievement)
                .isActive(true).createdDatetime(LocalDateTime.now()).lastUpdatedDatetime(LocalDateTime.now())
                .build();
        userAchievementRepository.save(ua);
        pointsService.addPoints(user, 50L);
    }

    // ===== HELPERS =====

    private void awardParticipationAchievements(Contest contest, List<Course> courses) {
        // Award "contest_participant" to every course owner
        courses.forEach(c -> {
            try { awardAchievement(c.getOwner().getUsername(), 1L); } catch (Exception ignored) {}
        });
        // Top 3 creators get "contest_winner"
        List<Course> ranked = courses.stream()
                .filter(c -> c.getRatingCount() > 0)
                .sorted((a, b) -> {
                    double avgA = (double) a.getRatingSum() / a.getRatingCount();
                    double avgB = (double) b.getRatingSum() / b.getRatingCount();
                    return Double.compare(avgB, avgA);
                }).collect(Collectors.toList());
        for (int i = 0; i < Math.min(3, ranked.size()); i++) {
            try { awardAchievement(ranked.get(i).getOwner().getUsername(), 2L + i); } catch (Exception ignored) {}
        }
    }

    private int getLevel(long points) {
        if (points < 100) return 1;
        return (int) (Math.sqrt(points / 100.0)) + 1;
    }

    private List<String> getValidTransitions(String current) {
        return switch (current) {
            case "ANNOUNCED" -> List.of("PREPARATION", "CANCELLED");
            case "PREPARATION" -> List.of("BUILDING", "ANNOUNCED", "CANCELLED");
            case "BUILDING" -> List.of("VALIDATION", "PREPARATION", "CANCELLED");
            case "VALIDATION" -> List.of("RESULTS", "BUILDING", "CANCELLED");
            case "RESULTS" -> List.of();
            case "CANCELLED" -> List.of("ANNOUNCED");
            default -> List.of();
        };
    }

    private ContestResponseDTO mapToDTO(Contest contest) {
        List<String> tagNames = contest.getTags() != null
                ? contest.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : List.of();
        return ContestResponseDTO.builder()
                .id(contest.getId()).title(contest.getTitle())
                .banner(contest.getBanner()).description(contest.getDescription())
                .theme(contest.getTheme()).difficulty(contest.getDifficulty())
                .content(contest.getContent()).tags(tagNames)
                .status(contest.getStatus())
                .startDate(contest.getStartDate())
                .preparationEndDate(contest.getPreparationEndDate())
                .buildingEndDate(contest.getBuildingEndDate())
                .validationEndDate(contest.getValidationEndDate())
                .endDate(contest.getEndDate())
                .externalLinks(contest.getExternalLinks())
                .bibliography(contest.getBibliography())
                .learningObjectives(contest.getLearningObjectives())
                .minLevel(contest.getMinLevel())
                .minReputation(contest.getMinReputation())
                .maxParticipants(contest.getMaxParticipants())
                .rewards(contest.getRewards())
                .participantCount(contest.getParticipantCount())
                .courseCount(contest.getCourseCount())
                .blockCount(contest.getBlockCount())
                .createdDatetime(contest.getCreatedDatetime())
                .build();
    }

    private ContestCourseResponseDTO buildContestCourseDTO(Course course) {
        List<CourseBlockResponseDTO> blockDTOs = courseBlockRepository.findByCourseIdOrderByBlockOrderAsc(course.getId())
                .stream().map(cb -> {
                    BlockVersion v = cb.getVersion() != null ? cb.getVersion() : cb.getBlock().getCurrentVersion();
                    return CourseBlockResponseDTO.builder()
                            .id(cb.getId()).blockId(cb.getBlock().getId())
                            .blockName(cb.getBlock().getName())
                            .version(BlockVersionResponseDTO.builder()
                                    .id(v.getId()).createdDatetime(v.getCreatedDatetime())
                                    .lastUpdatedDatetime(v.getLastUpdatedDatetime())
                                    .content(String.valueOf(v.getContent()))
                                    .versionNumber(v.getVersionNumber()).build())
                            .order(cb.getBlockOrder()).build();
                }).collect(Collectors.toList());

        double avg = course.getRatingCount() > 0 ? (double) course.getRatingSum() / course.getRatingCount() : 0.0;
        return ContestCourseResponseDTO.builder()
                .id(course.getId()).name(course.getName()).slug(course.getSlug())
                .tags(course.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .link(course.getLink()).orderIndex(0)
                .ratingSum(course.getRatingSum()).ratingCount(course.getRatingCount())
                .averageRating(Math.round(avg * 100.0) / 100.0)
                .createdDatetime(course.getCreatedDatetime()).blocks(blockDTOs).build();
    }

    private User findUserByUsername(String username) {
        var q = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true", User.class);
        q.setParameter("username", username);
        var r = q.getResultList();
        if (r.isEmpty()) throw new ResourceNotFoundException("user.not_found");
        return r.get(0);
    }

    private User requireAdmin(String username) {
        User user = findUserByUsername(username);
        if (!"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("contest.unauthorized");
        }
        return user;
    }

    private Set<Tag> resolveTagsByName(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return new HashSet<>();
        Set<String> normalized = tagNames.stream().map(String::trim).map(String::toLowerCase)
                .filter(n -> !n.isBlank()).collect(Collectors.toSet());
        if (normalized.isEmpty()) return new HashSet<>();
        List<Tag> existing = entityManager.createQuery(
                "SELECT t FROM Tag t WHERE t.name IN :names AND t.isActive = true", Tag.class)
                .setParameter("names", normalized).getResultList();
        Set<String> existingNames = existing.stream().map(Tag::getName).collect(Collectors.toSet());
        Set<Tag> result = new HashSet<>(existing);
        for (String name : normalized) {
            if (!existingNames.contains(name)) {
                Tag newTag = Tag.builder().name(name).isActive(true)
                        .createdDatetime(LocalDateTime.now()).lastUpdatedDatetime(LocalDateTime.now()).build();
                entityManager.persist(newTag);
                result.add(newTag);
            }
        }
        return result;
    }
}
