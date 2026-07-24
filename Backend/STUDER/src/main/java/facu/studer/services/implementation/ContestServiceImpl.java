package facu.studer.services.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContestServiceImpl implements ContestService {

    private static final List<String> VALID_STATUSES = List.of("ANNOUNCED", "PREPARATION", "VALIDATION", "RESULTS", "CANCELLED");

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

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDefaultAchievements() {
        if (achievementRepository.count() > 0) return;
        var now = LocalDateTime.now();
        achievementRepository.save(Achievement.builder().name("contest_participant").description("Participated in a contest")
                .badgeUrl("/badges/participant.png").category("participation").criteria(null)
                .isActive(true).createdDatetime(now).lastUpdatedDatetime(now).build());
        achievementRepository.save(Achievement.builder().name("contest_winner_gold").description("Won 1st place in a contest")
                .badgeUrl("/badges/gold.png").category("winner").criteria(null)
                .isActive(true).createdDatetime(now).lastUpdatedDatetime(now).build());
        achievementRepository.save(Achievement.builder().name("contest_winner_silver").description("Won 2nd place in a contest")
                .badgeUrl("/badges/silver.png").category("winner").criteria(null)
                .isActive(true).createdDatetime(now).lastUpdatedDatetime(now).build());
        achievementRepository.save(Achievement.builder().name("contest_winner_bronze").description("Won 3rd place in a contest")
                .badgeUrl("/badges/bronze.png").category("winner").criteria(null)
                .isActive(true).createdDatetime(now).lastUpdatedDatetime(now).build());
    }

    // ===== ADMIN: CONTEST CRUD =====

    @Override
    @Transactional
    public ContestResponseDTO createContest(String username, ContestCreateRequestDTO request) {
        User admin = requireAdmin(username);
        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        LocalDateTime start = request.getStartDate();
        Integer prepH = request.getPreparationDurationHours() != null ? request.getPreparationDurationHours() : 0;
        Integer valH = request.getValidationDurationHours() != null ? request.getValidationDurationHours() : 0;
        LocalDateTime prepEnd = start.plusHours(prepH);
        LocalDateTime valEnd = prepEnd.plusHours(valH);

        JsonNode contentNode = null;
        if (request.getContent() != null && !request.getContent().isBlank()) {
            try { contentNode = new ObjectMapper().readTree(request.getContent()); } catch (Exception ignored) {}
        }

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content(contentNode)
                .tags(managedTags)
                .startDate(start)
                .preparationEndDate(prepEnd)
                .validationEndDate(valEnd)
                .status("ANNOUNCED")
                .minPoints(request.getMinPoints())
                .participantCount(0).courseCount(0).blockCount(0)
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
        contest.setDescription(request.getDescription());

        if (request.getContent() != null && !request.getContent().isBlank()) {
            try { contest.setContent(new ObjectMapper().readTree(request.getContent())); } catch (Exception ignored) {}
        }

        contest.setTags(managedTags);
        contest.setStartDate(request.getStartDate());

        LocalDateTime start = request.getStartDate() != null ? request.getStartDate() : contest.getStartDate();
        if (request.getPreparationDurationHours() != null) {
            contest.setPreparationEndDate(start.plusHours(request.getPreparationDurationHours()));
        }
        if (request.getValidationDurationHours() != null) {
            LocalDateTime prepEnd = contest.getPreparationEndDate() != null ? contest.getPreparationEndDate() : start;
            contest.setValidationEndDate(prepEnd.plusHours(request.getValidationDurationHours()));
        }
        if (request.getMinPoints() != null) {
            contest.setMinPoints(request.getMinPoints());
        }

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

        contest.setStatus(newStatus);
        contest.setLastUpdatedDatetime(LocalDateTime.now());
        contestRepository.save(contest);

        return MessageDTO.builder().success(true).message("contest.status_changed").build();
    }

    @Override
    @Transactional
    public MessageDTO finishContest(String username, Long contestId) {
        requireAdmin(username);
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

    @Override
    @Transactional(readOnly = true)
    public Page<ContestResponseDTO> searchContests(String query, Pageable pageable) {
        return contestRepository.searchByTitle(query, pageable).map(this::mapToDTO);
    }

    // ===== PARTICIPATION =====

    @Override
    @Transactional
    public CourseResponseDTO submitCourseForContest(String username, Long contestId, CourseCreateRequestDTO request) {
        User owner = findUserByUsername(username);
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("contest.not_found"));

        if (!"PREPARATION".equals(contest.getStatus())) {
            throw new IllegalArgumentException("contest.not_accepting_submissions");
        }

        if (contest.getMinPoints() != null && owner.getPoints() < contest.getMinPoints()) {
            throw new IllegalArgumentException("contest.points_too_low");
        }

        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        String slug = request.getSlug() != null ? request.getSlug() : request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        Integer slugCount = courseRepository.countCoursesByName(request.getName());
        if (slugCount != null && slugCount > 0) slug = slug + slugCount;
        if (courseRepository.findBySlug(slug).isPresent()) slug = slug + System.currentTimeMillis();

        Course course = Course.builder()
                .owner(owner).name(request.getName()).slug(slug)
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
    public void awardAchievement(String username, String achievementName) {
        Achievement achievement = achievementRepository.findByName(achievementName)
                .orElseThrow(() -> new ResourceNotFoundException("achievement.not_found"));
        if (userAchievementRepository.existsByUserUsernameAndAchievementIdAndIsActiveTrue(username, achievement.getId())) return;
        User user = findUserByUsername(username);
        UserAchievement ua = UserAchievement.builder()
                .user(user).achievement(achievement)
                .isActive(true).createdDatetime(LocalDateTime.now()).lastUpdatedDatetime(LocalDateTime.now())
                .build();
        userAchievementRepository.save(ua);
        pointsService.addPoints(user, 50L);
    }

    // ===== HELPERS =====

    private void awardParticipationAchievements(Contest contest, List<Course> courses) {
        courses.forEach(c -> {
            try { awardAchievement(c.getOwner().getUsername(), "contest_participant"); } catch (Exception ignored) {}
        });
        List<Course> ranked = courses.stream()
                .filter(c -> c.getRatingCount() > 0)
                .sorted((a, b) -> {
                    double avgA = (double) a.getRatingSum() / a.getRatingCount();
                    double avgB = (double) b.getRatingSum() / b.getRatingCount();
                    return Double.compare(avgB, avgA);
                }).collect(Collectors.toList());
        String[] medals = {"contest_winner_gold", "contest_winner_silver", "contest_winner_bronze"};
        for (int i = 0; i < Math.min(3, ranked.size()); i++) {
            try { awardAchievement(ranked.get(i).getOwner().getUsername(), medals[i]); } catch (Exception ignored) {}
        }
    }

    private List<String> getValidTransitions(String current) {
        return switch (current) {
            case "ANNOUNCED" -> List.of("PREPARATION", "CANCELLED");
            case "PREPARATION" -> List.of("VALIDATION", "ANNOUNCED", "CANCELLED");
            case "VALIDATION" -> List.of("RESULTS", "PREPARATION", "CANCELLED");
            case "RESULTS" -> List.of();
            case "CANCELLED" -> List.of("ANNOUNCED");
            default -> List.of();
        };
    }

    private ContestResponseDTO mapToDTO(Contest contest) {
        List<String> tagNames = contest.getTags() != null
                ? contest.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : List.of();

        Integer prepH = 0;
        if (contest.getStartDate() != null && contest.getPreparationEndDate() != null) {
            prepH = (int) java.time.Duration.between(contest.getStartDate(), contest.getPreparationEndDate()).toHours();
        }
        Integer valH = 0;
        if (contest.getPreparationEndDate() != null && contest.getValidationEndDate() != null) {
            valH = (int) java.time.Duration.between(contest.getPreparationEndDate(), contest.getValidationEndDate()).toHours();
        }

        return ContestResponseDTO.builder()
                .id(contest.getId()).title(contest.getTitle())
                .description(contest.getDescription())
                .content(contest.getContent() != null ? contest.getContent().toString() : null)
                .status(contest.getStatus())
                .startDate(contest.getStartDate())
                .preparationEndDate(contest.getPreparationEndDate())
                .validationEndDate(contest.getValidationEndDate())
                .preparationDurationHours(prepH)
                .validationDurationHours(valH)
                .minPoints(contest.getMinPoints())
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
