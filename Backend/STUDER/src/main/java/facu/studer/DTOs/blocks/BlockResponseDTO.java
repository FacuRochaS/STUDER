package facu.studer.DTOs.blocks;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import facu.studer.DTOs.user.UserPublicResponseDTO;

import facu.studer.entities.blocks.BlockVersion;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BlockResponseDTO {

    private Long id;

    private LocalDateTime createdDatetime;

    private LocalDateTime lastUpdatedDatetime;

    private UserPublicResponseDTO owner;

    private Boolean isFork;

    private String name;

    private String slug;

    private String difficulty;

    private List<String> tags;

    private BlockVersionResponseDTO version;

    private long likeCount;

    private boolean likedByCurrentUser;

}
