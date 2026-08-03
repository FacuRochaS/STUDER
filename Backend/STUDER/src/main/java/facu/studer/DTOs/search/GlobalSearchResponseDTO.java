package facu.studer.DTOs.search;

import facu.studer.DTOs.blocks.BlockResponseDTO;
import facu.studer.DTOs.contest.ContestResponseDTO;
import facu.studer.DTOs.courses.CourseResponseDTO;
import facu.studer.DTOs.user.UserPublicResponseDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResponseDTO {
    private List<UserPublicResponseDTO> users;
    private List<BlockResponseDTO> blocks;
    private List<CourseResponseDTO> courses;
    private List<ContestResponseDTO> contests;
}
