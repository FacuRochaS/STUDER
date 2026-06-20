package facu.studer.repositories.chat;

import facu.studer.entities.messages.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // Busca un chat existente entre dos usuarios (sin importar el orden)
    @Query("SELECT c FROM Chat c WHERE ((c.user1.id = :user1Id AND c.user2.id = :user2Id) " +
            "OR (c.user1.id = :user2Id AND c.user2.id = :user1Id)) AND c.isActive = true")
    Optional<Chat> findChatBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // Obtiene todos los chats activos de un usuario
    @Query("SELECT c FROM Chat c WHERE (c.user1.id = :userId OR c.user2.id = :userId) AND c.isActive = true")
    List<Chat> findChatsByUserId(@Param("userId") Long userId);
}