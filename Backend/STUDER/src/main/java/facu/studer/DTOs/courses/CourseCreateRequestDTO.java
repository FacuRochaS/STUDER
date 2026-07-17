package facu.studer.DTOs.courses;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourseCreateRequestDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String link;

    private List<String> tags;

    private List<CourseBlockRequestDTO> blocks;
}
