package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.notificationDtos.NotificationResponseDto;
import org.guram.eventscheduler.exceptions.ForbiddenOperationException;
import org.guram.eventscheduler.exceptions.ResourceNotFoundException;
import org.guram.eventscheduler.models.Notification;
import org.guram.eventscheduler.models.NotificationType;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepo;

    @InjectMocks
    private NotificationService notificationService;


    @Test
    void createNotification_shouldSucceed_whenValidInput() {
        User recipient = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        notificationService.createNotification(recipient, "notification message", NotificationType.EVENT_CANCELLED);

        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(notificationArgumentCaptor.capture());

        Notification savedNotification = notificationArgumentCaptor.getValue();
        assertThat(savedNotification.getRecipient()).isEqualTo(recipient);
        assertThat(savedNotification.getMessage()).isEqualTo("notification message");
        assertThat(savedNotification.getType()).isEqualTo(NotificationType.EVENT_CANCELLED);
    }

    @Test
    void getNotificationsForUser_shouldReturnNotifications_whenValidUser() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setMessage("test message");
        notification.setType(NotificationType.EVENT_DETAILS_UPDATED);

        when(notificationRepo.findByRecipientOrderByCreatedAtDesc(user)).thenReturn(List.of(notification));

        List<NotificationResponseDto> result = notificationService.getNotificationsForUser(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).message()).isEqualTo("test message");
    }

    @Test
    void markNotificationAsRead_shouldThrowException_whenNotificationNotFound() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(notificationRepo.getNotificationById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markNotificationAsRead(1L, user));
    }

    @Test
    void markNotificationAsRead_shouldThrowException_whenUserIsNotRecipient() {
        User owner = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        owner.setId(1L);
        User other = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        other.setId(2L);

        Notification notification = new Notification();
        notification.setRecipient(owner);

        when(notificationRepo.getNotificationById(1L)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenOperationException.class, () -> notificationService.markNotificationAsRead(1L, other));

        verify(notificationRepo, never()).save(any(Notification.class));
    }

    @Test
    void markNotificationAsRead_shouldDoNothing_whenAlreadyRead() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setRead(true);

        when(notificationRepo.getNotificationById(1L)).thenReturn(Optional.of(notification));

        notificationService.markNotificationAsRead(1L, user);

        verify(notificationRepo, never()).save(any(Notification.class));
    }

    @Test
    void markNotificationAsRead_shouldSucceed_whenValidInput() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setRead(false);

        when(notificationRepo.getNotificationById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepo.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.markNotificationAsRead(1L, user);

        ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(notificationArgumentCaptor.capture());

        Notification savedNotification = notificationArgumentCaptor.getValue();
        assertThat(savedNotification.isRead()).isTrue();
    }

    @Test
    void markAllNotificationAsRead_shouldSucceed_whenValidUser() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        notificationService.markAllNotificationAsRead(user);

        verify(notificationRepo).markAllAsReadForUser(user);
    }

}
