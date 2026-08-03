package facu.studer.DTOs.courses;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourseResponseDTO {
    private Long id;
    private UserPublicResponseDTO owner;
    private String name;
    private String slug;
    private List<String> tags;
    private String link;
    private Boolean published;
    private LocalDateTime createdDatetime;
    private LocalDateTime lastUpdatedDatetime;
    private Boolean favourite;
    private Long favouriteCount;
    private Long ratingSum;
    private Integer ratingCount;
    private List<CourseBlockResponseDTO> blocks;
}
