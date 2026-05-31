package facu.studer.DTOs.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated friends list response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FriendsListResponseDTO {

    /**
     * List of friends.
     */
    private List<FriendResponseDTO> friends;

    /**
     * Total number of elements.
     */
    private Long totalElements;

    /**
     * Whether there are more pages.
     */
    private Boolean hasMore;

    /**
     * Current page number.
     */
    private Integer currentPage;
}

