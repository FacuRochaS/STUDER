package facu.studer.DTOs.courses;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.DTOs.blocks.BlockVersionResponseDTO;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourseBlockResponseDTO {
    private Long id;
    private Long blockId;
    private String blockName;
    private BlockVersionResponseDTO version;
    private Integer order;
}
