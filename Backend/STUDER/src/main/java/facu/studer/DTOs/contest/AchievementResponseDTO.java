package facu.studer.DTOs.contest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AchievementResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String badgeUrl;
    private String category;
    private JsonNode criteria;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
}
