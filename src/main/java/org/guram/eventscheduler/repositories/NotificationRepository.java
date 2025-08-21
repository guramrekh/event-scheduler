package org.guram.eventscheduler.repositories;

import jakarta.transaction.Transactional;
import org.guram.eventscheduler.models.Notification;
import org.guram.eventscheduler.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    Optional<Notification> getNotificationById(Long notificationId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n
        SET n.read = true
        WHERE n.recipient = :user
            AND n.read = false
    """)
    void markAllAsReadForUser(@Param("user") User user);
}
