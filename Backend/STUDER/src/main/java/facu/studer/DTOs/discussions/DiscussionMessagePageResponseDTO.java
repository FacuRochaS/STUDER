package facu.studer.DTOs.discussions;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DiscussionMessagePageResponseDTO {

    private List<DiscussionMessageResponseDTO> messages;

    @JsonProperty("total_elements")
    private long totalElements;

    @JsonProperty("has_more")
    private boolean hasMore;

    @JsonProperty("current_page")
    private int currentPage;
}

