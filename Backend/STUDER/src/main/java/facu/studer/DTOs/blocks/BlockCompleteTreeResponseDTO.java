package facu.studer.DTOs.blocks;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BlockCompleteTreeResponseDTO {
    BlockCompleteResponseDTO parent;

    BlockCompleteResponseDTO block;

    List<BlockResponseDTO> sons;
}
