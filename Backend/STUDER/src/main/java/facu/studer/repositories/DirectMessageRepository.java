package facu.studer.repositories;

import facu.studer.entities.User;
import facu.studer.entities.messages.DirectMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;


import java.util.Optional;
/**
 * Repository interface for DirectMessage entity.
 * Provides CRUD operations and query methods for direct messages.
 */
@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    // Obtiene mensajes ordenados por fecha (descendente para obtener los últimos primero)
    Page<DirectMessage> findByChatIdAndIsActiveTrueOrderByCreatedDatetimeDesc(Long chatId, Pageable pageable);

    // Obtiene el último mensaje de un chat
    Optional<DirectMessage> findFirstByChatIdAndIsActiveTrueOrderByCreatedDatetimeDesc(Long chatId);

    // Marca como leídos los mensajes de un chat que no fueron enviados por el usuario actual
    @Modifying
    @Query("UPDATE DirectMessage m SET m.isRead = true WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.isRead = false AND m.isActive = true")
    int markUnreadMessagesAsRead(@Param("chatId") Long chatId, @Param("userId") Long userId);
}