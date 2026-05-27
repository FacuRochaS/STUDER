package facu.studer.DTOs.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for friend response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FriendResponseDTO {

    /**
     * Friend relationship ID.
     */
    private Long id;

    /**
     * User ID of the friend.
     */
    private Long userId;

    /**
     * Username of the friend.
     */
    private String username;

    /**
     * First name of the friend.
     */
    private String firstName;

    /**
     * Last name of the friend.
     */
    private String lastName;

    /**
     * Email of the friend.
     */
    private String email;

    /**
     * Whether both users have accepted the friendship.
     */
    private Boolean isFriend;
}

