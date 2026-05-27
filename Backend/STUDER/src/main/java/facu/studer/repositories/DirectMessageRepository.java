package facu.studer.repositories;

import facu.studer.entities.User;
import facu.studer.entities.messages.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DirectMessage entity.
 * Provides CRUD operations and query methods for direct messages.
 */
@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    /**
     * Finds messages between two users ordered by sent time (descending).
     * @param user1 the first user
     * @param user2 the second user
     * @param pageable pagination info
     * @return page of messages between the two users
     */
    @Query("SELECT m FROM DirectMessage m " +
           "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
           "OR (m.sender = :user2 AND m.receiver = :user1) " +
           "AND m.isActive = true " +
           "ORDER BY m.sentAt DESC")
    Page<DirectMessage> findMessagesBetweenUsers(
            @Param("user1") User user1,
            @Param("user2") User user2,
            Pageable pageable);

    /**
     * Finds the last message between two users.
     * @param user1 the first user
     * @param user2 the second user
     * @return the most recent message
     */
    @Query("SELECT m FROM DirectMessage m " +
           "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
           "OR (m.sender = :user2 AND m.receiver = :user1) " +
           "AND m.isActive = true " +
           "ORDER BY m.sentAt DESC")
    List<DirectMessage> findLastMessageBetweenUsers(
            @Param("user1") User user1,
            @Param("user2") User user2,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Finds the last message between two users (convenience method).
     * @param user1 the first user
     * @param user2 the second user
     * @return the most recent message
     */
    default Optional<DirectMessage> findLastMessageBetweenUsers(User user1, User user2) {
        List<DirectMessage> messages = findLastMessageBetweenUsers(
                user1, user2, 
                org.springframework.data.domain.PageRequest.of(0, 1));
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
    }

    /**
     * Finds unread messages for a user.
     * @param receiver the receiver user
     * @return list of unread messages
     */
    List<DirectMessage> findByReceiverAndIsReadFalseAndIsActiveTrue(User receiver);

    /**
     * Gets paginated chat list for a user (with friends only).
     * Returns the last message from each conversation with a friend.
     * @param userId the user ID
     * @param pageable pagination info
     * @return page of users the user has chats with
     */
    @Query(value = "SELECT DISTINCT CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END as user_id " +
                   "FROM messages m " +
                   "WHERE (m.sender_id = :userId OR m.receiver_id = :userId) " +
                   "AND m.is_active = true " +
                   "ORDER BY m.sent_at DESC",
           countQuery = "SELECT COUNT(DISTINCT CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END) " +
                        "FROM messages m " +
                        "WHERE (m.sender_id = :userId OR m.receiver_id = :userId) " +
                        "AND m.is_active = true",
           nativeQuery = true)
    Page<?> findChatUsersForUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds messages sent by a specific user as replies to requests.
     * Used to get message requests from users who are not yet friends.
     * @param receiver the receiver user
     * @param pageable pagination info
     * @return page of messages from non-friends
     */
    @Query("SELECT m FROM DirectMessage m " +
           "WHERE m.receiver = :receiver " +
           "AND m.isActive = true " +
           "AND NOT EXISTS (" +
                   "SELECT 1 FROM Friend f " +
                   "WHERE ((f.sender = m.sender AND f.receiver = m.receiver) OR " +
                   "       (f.sender = m.receiver AND f.receiver = m.sender)) " +
                   "AND f.senderAccept = true " +
                   "AND f.receiverAccept = true" +
           ") " +
           "ORDER BY m.sentAt DESC")
    Page<DirectMessage> findMessageRequestsForUser(@Param("receiver") User receiver, Pageable pageable);
}

