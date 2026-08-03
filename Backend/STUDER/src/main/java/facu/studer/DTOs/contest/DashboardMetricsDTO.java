package facu.studer.DTOs.contest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DashboardMetricsDTO {
    private long totalUsers;
    private long activeToday;
    private long activeThisWeek;
    private long newRegistrations;
    private long totalBlocks;
    private long totalCourses;
    private long totalForks;
    private long totalVersions;
    private long totalLikes;
    private long totalComments;
    private long activeContests;
    private long finishedContests;
    private List<DifficultyDistributionDTO> difficultyDistribution;
    private List<TagDistributionDTO> topTags;
    private List<UserReputationDTO> topUsers;
    private List<DifficultyDistributionDTO> blockTypeDistribution;
}
