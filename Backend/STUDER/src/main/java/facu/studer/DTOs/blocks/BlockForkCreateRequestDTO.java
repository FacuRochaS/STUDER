package facu.studer.DTOs.blocks;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.entities.blocks.Block;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BlockForkCreateRequestDTO {

    private List<String> tags;

    private String name;

    private String slug;

    private String difficulty;

    private String content;

    private Long BlockId;

    private Boolean published;

}
