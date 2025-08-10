package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.notificationDtos.NotificationResponseDto;
import org.guram.eventscheduler.exceptions.ForbiddenOperationException;
import org.guram.eventscheduler.exceptions.ResourceNotFoundException;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.models.Notification;
import org.guram.eventscheduler.models.NotificationType;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.NotificationRepository;
import org.guram.eventscheduler.utils.EntityToDtoMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;

    @Autowired
    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }


    @Transactional
    public void createNotification(User recipient, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        notificationRepo.save(notification);
    }

    public List<NotificationResponseDto> getNotificationsForUser(User user) {
        return notificationRepo.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(EntityToDtoMappings::mapNotificationToResponseDto)
                .toList();
    }

    public void markNotificationAsRead(Long notificationId, User user) {
        Notification notification = notificationRepo.getNotificationById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (notification.getRecipient() != user)
            throw new ForbiddenOperationException("User (ID=" + user.getId() + ") is not authorized for this notification.");

        if (notification.isRead())
            return;

        notification.setRead(true);
        notificationRepo.save(notification);
    }

    public void markAllNotificationAsRead(User user) {
        notificationRepo.markAllAsReadForUser(user);
    }

    public String generateInvitationMessage(Event event) {
        return String.format("You have been invited to '%s'", event.getTitle());
    }
    public String generateEventCancelledMessage(Event event) {
        return String.format("'%s' has been cancelled.", event.getTitle());
    }
    public String generateEventUpdatedMessage(Event event) {
        return String.format("The details for '%s' have been updated.", event.getTitle());
    }
    public String generateInvitationResponseMessage(User invitee, Event event, InvitationStatus response) {
        return String.format("%s %s has %s your invitation to '%s'",
                invitee.getFirstName(), invitee.getLastName(),
                response.toString().toLowerCase(),
                event.getTitle());
    }
    public String generateAddedAsOrganizerMessage(User invitor, Event event) {
        return String.format("%s %s has added you as an organizer to '%s'",
                invitor.getFirstName(),
                invitor.getLastName(),
                event.getTitle());
    }
    public String generateRemovedAsOrganizerMessage(User invitor, Event event) {
        return String.format("%s %s has removed you as an organizer from '%s'",
                invitor.getFirstName(),
                invitor.getLastName(),
                event.getTitle());
    }
    public String generateKickedOutFromEventMessage(User organizer, Event event) {
        return String.format("%s %s has kicked you out of '%s'",
                organizer.getFirstName(),
                organizer.getLastName(),
                event.getTitle());
    }

}
