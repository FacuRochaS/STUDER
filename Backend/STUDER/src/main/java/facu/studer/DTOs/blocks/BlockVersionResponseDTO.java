package facu.studer.DTOs.blocks;

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
public class BlockVersionResponseDTO {

    private Long id;

    private LocalDateTime createdDatetime;

    private LocalDateTime lastUpdatedDatetime;

    private String content;

    private Long versionNumber;

    private String changeDescription;
}
