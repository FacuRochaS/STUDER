package facu.studer.services.implementation;

import facu.studer.DTOs.media.ImageUploadResponseDTO;
import facu.studer.DTOs.user.*;
import facu.studer.entities.LinkedType;
import facu.studer.entities.users.User;
import facu.studer.mappers.UserMapper;
import facu.studer.repositories.FriendRepository;
import facu.studer.repositories.UserRepository;
import facu.studer.services.support.NewNotificationService;
import facu.studer.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final String mediaServiceUrl;


    private final NewNotificationService newNotificationService;
    private final FriendRepository friendRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RestTemplate restTemplate,
                           @Value("${app.media.service.url}") String mediaServiceUrl, NewNotificationService newNotificationService, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.mediaServiceUrl = mediaServiceUrl;


        this.newNotificationService = newNotificationService;
        this.friendRepository = friendRepository;
    }

    @Override
    @Transactional
    public UserResponseDTO create(UserCreateRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("user.email.unique");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("user.username.unique");
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedDatetime(LocalDateTime.now());
        user.setLastUpdatedDatetime(LocalDateTime.now());
        user.setIsActive(true);

        User saved = userRepository.save(user);
        newNotificationService.createNotification(
                saved.getId(),
                "user.create.message",
                "user.create.welcome",
                LinkedType.USER,
                saved.getId());
        return UserMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public UserResponseDTO update(String currentUsername, UserUpdateRequestDTO request, MultipartFile file) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new IllegalArgumentException("user.not_found");
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (file != null && !file.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", file.getResource());

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                String url = mediaServiceUrl + "/upload-image/profile";
                logger.info("Calling media service at URL: {}", url);
                ImageUploadResponseDTO response = restTemplate.postForObject(url, requestEntity, ImageUploadResponseDTO.class);

                if (response != null && response.getUrls() != null) {
                    logger.info("Received URLs from media service: {}", response.getUrls());
                    currentUser.setProfilePictureOriginalUrl(response.getUrls().get("original"));
                    currentUser.setProfilePictureAvatarUrl(response.getUrls().get("avatar"));
                    currentUser.setProfilePictureWebpUrl(response.getUrls().get("webp"));
                    currentUser.setProfilePictureThumbnailUrl(response.getUrls().get("thumbnail"));
                } else {
                    logger.warn("Media service returned a null or empty response.");
                }
            } catch (Exception e) {
                logger.error("🚨 Failed to call media service to upload image for user {}", currentUsername, e);
            }
        }

        currentUser.setLastUpdatedDatetime(LocalDateTime.now());
        User updated = userRepository.save(currentUser);
        return UserMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public UserResponseDTO softDelete(String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new IllegalArgumentException("user.not_found");
        }

        currentUser.setIsActive(false);
        currentUser.setLastUpdatedDatetime(LocalDateTime.now());
        User deleted = userRepository.save(currentUser);
        return UserMapper.toResponseDTO(deleted);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPublicResponseDTO getById(Long id) {
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("user.not_found");
        }
        Long followers = friendRepository.countAllByReceiverOrSender(user.get(),user.get());
        return UserMapper.toPublicResponseDTO(user.get(), followers);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("user.not_found");
        }
        return UserMapper.toResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPublicResponseDTO getPublicByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("user.not_found");
        }
        Long followers = friendRepository.countAllByReceiverOrSender(user,user);
        return UserMapper.toPublicResponseDTO(user, followers);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSearchPageResponseDTO searchByUsername(String query, int page, int size) {
        String normalized = query == null ? "" : query.trim();
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByUsernameContainingIgnoreCase(normalized, pageable);

        List<UserPublicResponseDTO> users = new ArrayList<>();
        for (User user : userPage.getContent()) {
            users.add(UserMapper.toPublicResponseDTO(user, friendRepository.countAllByReceiverOrSender(user, user)));
        }

        return UserSearchPageResponseDTO.builder()
                .users(users)
                .totalElements(userPage.getTotalElements())
                .hasMore(userPage.hasNext())
                .currentPage(userPage.getNumber())
                .build();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}