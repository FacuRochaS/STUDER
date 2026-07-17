package facu.studer.DTOs.feed;

import com.fasterxml.jackson.databind.JsonNode;
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
public class PostResponseDTO {
    private Long id;
    private UserPublicResponseDTO user;
    private JsonNode content;
    private List<String> tags;
    private LocalDateTime createdDatetime;
    private long likeCount;
    private boolean likedByCurrentUser;
}
