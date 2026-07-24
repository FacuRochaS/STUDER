package facu.studer.services;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.DTOs.courses.CourseCreateRequestDTO;
import facu.studer.DTOs.courses.CourseResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContestService {
    // Public
    ContestResponseDTO getContest(Long contestId);
    Page<ContestResponseDTO> listContests(String status, Pageable pageable);
    Page<ContestResponseDTO> searchContests(String query, Pageable pageable);
    List<ContestResponseDTO> getActiveContests();
    List<ContestResponseDTO> getUpcomingContests();

    // Participation
    CourseResponseDTO submitCourseForContest(String username, Long contestId, CourseCreateRequestDTO request);
    ContestCourseResponseDTO getRandomCourseForValidation(String username, Long contestId);
    MessageDTO rateCourse(String username, CourseRatingRequestDTO request);
    List<LeaderboardEntryDTO> getLeaderboard(Long contestId);

    // Admin
    ContestResponseDTO createContest(String username, ContestCreateRequestDTO request);
    ContestResponseDTO updateContest(String username, Long contestId, ContestCreateRequestDTO request);
    MessageDTO deleteContest(String username, Long contestId);
    MessageDTO changeContestStatus(String username, Long contestId, String newStatus);
    MessageDTO finishContest(String username, Long contestId);

    // Achievements
    List<AchievementResponseDTO> getUserAchievements(String username);
    List<AchievementResponseDTO> getAllAchievements();
}
