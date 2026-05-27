package facu.studer.repositories;

import facu.studer.entities.Friend;
import facu.studer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Friend entity.
 * Provides CRUD operations and query methods for friend relationships.
 */
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * Finds a friend relationship between two users (either direction).
     * @param user1 the first user
     * @param user2 the second user
     * @return the Friend relationship if exists
     */
    @Query("SELECT f FROM Friend f " +
           "WHERE (f.sender = :user1 AND f.receiver = :user2) " +
           "OR (f.sender = :user2 AND f.receiver = :user1)")
    Optional<Friend> findFriendship(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Finds a friend relationship where user2 is the sender and user1 is the receiver.
     * @param sender the sender user
     * @param receiver the receiver user
     * @return the Friend relationship if exists
     */
    Optional<Friend> findBySenderAndReceiver(User sender, User receiver);

    /**
     * Finds confirmed friends (both accepted) for a user.
     * @param user the user
     * @return list of friend relationships where both users have accepted
     */
    @Query("SELECT f FROM Friend f " +
           "WHERE (f.sender = :user OR f.receiver = :user) " +
           "AND f.senderAccept = true " +
           "AND f.receiverAccept = true")
    List<Friend> findConfirmedFriends(@Param("user") User user);

    /**
     * Gets the opposite user in a friend relationship.
     * @param friendId the friend relationship ID
     * @param userId the user ID to get the opposite of
     * @return the opposite user in the relationship
     */
    @Query("SELECT CASE WHEN f.sender.id = :userId THEN f.receiver ELSE f.sender END " +
           "FROM Friend f " +
           "WHERE f.id = :friendId")
    Optional<User> getOtherUserInFriendship(@Param("friendId") Long friendId, @Param("userId") Long userId);
}

