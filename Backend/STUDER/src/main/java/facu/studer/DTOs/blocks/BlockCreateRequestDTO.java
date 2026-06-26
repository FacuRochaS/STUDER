package facu.studer.DTOs.blocks;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.entities.blocks.Difficulty;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BlockCreateRequestDTO {

    private List<String> tags;

    private String name;

    private String slug;

    private String difficulty;

    private String content;

    private Boolean published;

}
