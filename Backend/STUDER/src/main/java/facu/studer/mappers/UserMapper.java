package facu.studer.mappers;

import facu.studer.DTOs.user.UserCreateRequestDTO;
import facu.studer.DTOs.user.UserResponseDTO;
import facu.studer.entities.User;

/**
 * Mapper for User entity and related DTOs.
 */
public final class UserMapper {
    private UserMapper() { }

    /**
     * Maps a UserCreateRequestDTO to a User entity.
     * @param dto the create request DTO
     * @return the User entity
     */
    public static User toEntity(UserCreateRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .birthDate(dto.getBirthDate())
                .build();
    }


    /**
     * Maps a User entity to a UserResponseDTO.
     * @param user the User entity
     * @return the response DTO
     */
    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
