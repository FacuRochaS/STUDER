package facu.studer.services.implementation;

import facu.studer.DTOs.friends.FriendResponseDTO;
import facu.studer.DTOs.friends.FriendStatusResponseDTO;
import facu.studer.DTOs.friends.FriendsListResponseDTO;
import facu.studer.entities.Friend;
import facu.studer.entities.LinkedType;
import facu.studer.entities.User;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.FriendMapper;
import facu.studer.repositories.FriendRepository;
import facu.studer.repositories.UserRepository;
import facu.studer.services.FriendService;
import facu.studer.services.support.NewNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of FriendService.
 * Manages friend relationships and notifications.
 */
@Service
public class FriendServiceImpl implements FriendService {

    private static final int PAGE_SIZE = 10;

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    private final NewNotificationService newNotificationService;


    public FriendServiceImpl(
            FriendRepository friendRepository,
            UserRepository userRepository,
            NewNotificationService newNotificationService) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;

        this.newNotificationService = newNotificationService;
    }

    /**
     * Follow user logic:
     * 1. Check if target user has already sent a request (sender=target, receiver=current)
     * 2. If yes, accept it (set both flags to true)
     * 3. If no, check for existing (sender=current, receiver=target)
     * 4. If exists, do nothing (already following)
     * 5. Otherwise, create new relationship as sender
     */
    @Override
    @Transactional
    public FriendResponseDTO followUser(String currentUsername, Long targetUserId) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("user.not_found"));

        if (currentUser.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("friend.cannot_follow_yourself");
        }

        // Check if target has already sent a request
        Optional<Friend> existingRequest = friendRepository.findBySenderAndReceiver(targetUser, currentUser);
        
        Friend friendshipRecord;
        boolean isNewFollowing = false;

        if (existingRequest.isPresent()) {
            // Accept the existing request from target
            friendshipRecord = existingRequest.get();
            friendshipRecord.setReceiverAccept(true);
            friendshipRecord.setLastUpdatedDatetime(LocalDateTime.now());
        } else {
            // Check if current user has already sent a request
            Optional<Friend> currentRequest = friendRepository.findBySenderAndReceiver(currentUser, targetUser);
            if (currentRequest.isPresent()) {
                friendshipRecord = currentRequest.get();
                // Already following, do nothing
            } else {
                // Create new request with current user as sender
                friendshipRecord = Friend.builder()
                        .sender(currentUser)
                        .senderAccept(true)
                        .receiver(targetUser)
                        .receiverAccept(false)
                        .createdDatetime(LocalDateTime.now())
                        .lastUpdatedDatetime(LocalDateTime.now())
                        .isActive(true)
                        .build();
                isNewFollowing = true;
            }
        }

        friendshipRecord = friendRepository.save(friendshipRecord);

        // Create notification for the receiver
        if (isNewFollowing || existingRequest.isPresent()) {
            createFollowNotification(targetUser, currentUser);
        }

        return FriendMapper.toResponseDTO(friendshipRecord, currentUser.getId());
    }

    /**
     * Unfollow user by setting both accept flags to false.
     */
    @Override
    @Transactional
    public Boolean unfollowUser(String currentUsername, Long targetUserId) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("user.not_found"));

        Optional<Friend> friendship = friendRepository.findFriendship(currentUser, targetUser);

        if (friendship.isPresent()) {
            Friend friend = friendship.get();
            friend.setSenderAccept(false);
            friend.setReceiverAccept(false);
            friend.setLastUpdatedDatetime(LocalDateTime.now());
            friendRepository.save(friend);
            return true;
        }

        return false;
    }

    /**
     * Gets confirmed friends for a user (both accept flags are true).
     */
    @Override
    @Transactional(readOnly = true)
    public FriendsListResponseDTO getFriends(String currentUsername, int page) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        List<Friend> friendships = friendRepository.findConfirmedFriends(currentUser);

        // Convert to DTOs
        List<FriendResponseDTO> friends = friendships.stream()
                .map(f -> FriendMapper.toResponseDTO(f, currentUser.getId()))
                .collect(Collectors.toList());

        // Manual pagination
        int totalElements = friends.size();
        int pageSize = PAGE_SIZE;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);

        List<FriendResponseDTO> pageContent = friends.subList(
                Math.min(startIndex, totalElements),
                endIndex
        );

        return FriendsListResponseDTO.builder()
                .friends(pageContent)
                .totalElements((long) totalElements)
                .hasMore(endIndex < totalElements)
                .currentPage(page)
                .build();
    }

    /**
     * Creates a USER type notification for when a user is followed.
     */
    private void createFollowNotification(User receiver, User follower) {

        newNotificationService.createNotification(
                receiver.getId(),
                "friend.follow_title",
                "friend.follow_message",
                LinkedType.USER,
                follower.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FriendStatusResponseDTO getFriendStatus(String currentUsername, Long targetUserId) {
        User currentUser = userRepository.findByUsername(currentUsername);
        if (currentUser == null) {
            throw new ResourceNotFoundException("user.not_found");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("user.not_found"));

        Optional<Friend> friendship = friendRepository.findFriendship(currentUser, targetUser);
        if (friendship.isEmpty()) {
            return FriendStatusResponseDTO.builder()
                    .isFollowing(false)
                    .isFriend(false)
                    .build();
        }

        Friend friend = friendship.get();
        boolean currentIsSender = friend.getSender().getId().equals(currentUser.getId());
        boolean isFollowing = currentIsSender
                ? Boolean.TRUE.equals(friend.getSenderAccept())
                : Boolean.TRUE.equals(friend.getReceiverAccept());
        boolean isFriend = Boolean.TRUE.equals(friend.getSenderAccept())
                && Boolean.TRUE.equals(friend.getReceiverAccept());

        return FriendStatusResponseDTO.builder()
                .isFollowing(isFollowing)
                .isFriend(isFriend)
                .build();
    }
}

