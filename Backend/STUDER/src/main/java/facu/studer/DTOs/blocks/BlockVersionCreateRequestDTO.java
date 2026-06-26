package facu.studer.DTOs.blocks;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BlockVersionCreateRequestDTO {

    private String content;

    private String changeDescription;

    private Long BlockId;

    private Boolean published;
}
