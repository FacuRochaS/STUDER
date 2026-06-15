package facu.studer.DTOs.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public user data for profiles and searches.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserPublicResponseDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String profilePictureOriginalUrl;
    private String profilePictureAvatarUrl;
    private String profilePictureWebpUrl;
    private String profilePictureThumbnailUrl;
}

