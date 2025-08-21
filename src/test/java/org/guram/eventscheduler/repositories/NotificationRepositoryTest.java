package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.Notification;
import org.guram.eventscheduler.models.NotificationType;
import org.guram.eventscheduler.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;

    @BeforeEach
    void setUp() {
        user1 = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        User user2 = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        userRepository.saveAll(List.of(user1, user2));

        Notification notification1 = new Notification();
        notification1.setRecipient(user1);
        notification1.setMessage("notification message one");
        notification1.setType(NotificationType.EVENT_DETAILS_UPDATED);
        notification1.setRead(false);

        Notification notification2 = new Notification();
        notification2.setRecipient(user1);
        notification2.setMessage("notification message two");
        notification2.setType(NotificationType.EVENT_CANCELLED);
        notification2.setRead(true);

        Notification notification3 = new Notification();
        notification3.setRecipient(user1);
        notification3.setMessage("notification message three");
        notification3.setType(NotificationType.EVENT_INVITATION_RECEIVED);
        notification3.setRead(false);

        Notification notification4 = new Notification();
        notification4.setRecipient(user2);
        notification4.setMessage("notification for user two");
        notification4.setType(NotificationType.EVENT_DETAILS_UPDATED);
        notification4.setRead(false);

        notificationRepository.saveAll(List.of(notification1, notification2, notification3, notification4));
    }


    @Test
    void findByRecipientOrderByCreatedAtDesc_shouldReturnCorrectNotificationsOrdered_whenRecipientExistsInDb() {
        List<Notification> foundNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user1);

        assertThat(foundNotifications)
                .hasSize(3)
                .allMatch(n -> n.getRecipient().equals(user1));
    }

    @Test
    void findByRecipientOrderByCreatedAtDesc_shouldReturnEmptyList_whenRecipientHasNoNotifications() {
        User user = new User("carol", "brown", "carol.brown@email.com", "<PASSWORD>");
        userRepository.save(user);

        List<Notification> foundNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);

        assertThat(foundNotifications).isEmpty();
    }

    @Test
    void getNotificationById_shouldReturnNotification_whenIdExistsInDb() {
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user1);
        Long notificationId = notifications.get(0).getId();

        Optional<Notification> optionalNotification = notificationRepository.getNotificationById(notificationId);

        assertThat(optionalNotification)
                .isPresent()
                .get()
                .extracting(Notification::getId)
                .isEqualTo(notificationId);
    }

    @Test
    void getNotificationById_shouldReturnEmptyOptional_whenIdNotExistsInDb() {
        Optional<Notification> optionalNotification = notificationRepository.getNotificationById(-999L);

        assertThat(optionalNotification).isEmpty();
    }

    @Test
    void markAllAsReadForUser_shouldUpdateOnlyUnreadNotificationsForSpecificUser_whenCalled() {
        notificationRepository.markAllAsReadForUser(user1);

        List<Notification> notifications = user1.getNotifications();

        assertThat(notifications).allMatch(Notification::isRead);
    }

}
