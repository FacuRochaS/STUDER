package facu.studer.services;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.contest.*;
import facu.studer.DTOs.courses.CourseCreateRequestDTO;
import facu.studer.DTOs.courses.CourseResponseDTO;

import java.util.List;

public interface ContestService {
    ContestResponseDTO createContest(String username, ContestCreateRequestDTO request);
    ContestResponseDTO getContest(Long contestId);
    CourseResponseDTO submitCourseForContest(String username, Long contestId, CourseCreateRequestDTO request);
    ContestCourseResponseDTO getRandomCourseForValidation(String username, Long contestId);
    MessageDTO rateCourse(String username, CourseRatingRequestDTO request);
    MessageDTO finishContest(String username, Long contestId);
    List<LeaderboardEntryDTO> getLeaderboard(Long contestId);
}
