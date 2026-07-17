package facu.studer.DTOs.contest;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.DTOs.courses.CourseBlockResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContestCourseResponseDTO {
    private Long id;
    private String name;
    private String slug;
    private List<String> tags;
    private String link;
    private Integer orderIndex;
    private Long ratingSum;
    private Integer ratingCount;
    private Double averageRating;
    private LocalDateTime createdDatetime;
    private List<CourseBlockResponseDTO> blocks;
}
