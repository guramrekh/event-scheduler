package org.guram.eventscheduler.services;

import org.guram.eventscheduler.DTOs.notificationDTOs.NotificationResponseDto;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.*;
import org.guram.eventscheduler.repositories.NotificationRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    @Autowired
    public NotificationService(NotificationRepository notificationRepo, UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }


    @Transactional
    public Notification createNotification(User recipient, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepo.save(notification);
    }

    public List<NotificationResponseDto> getNotificationsForUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return notificationRepo.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(Utils::mapNotificationToResponseDto)
                .collect(Collectors.toList());
    }

    public String generateInvitationMessage(Event event) {
        return String.format("You have been invited to the event: %s", event.getTitle());
    }

    public String generateEventCancelledMessage(Event event) {
        return String.format("The event '%s' has been cancelled.", event.getTitle());
    }

    public String generateEventUpdatedMessage(Event event) {
        return String.format("The details for the event '%s' have been updated.", event.getTitle());
    }

    public String generateInvitationResponseMessage(User invitee, Event event, InvitationStatus response) {
        return String.format("%s %s has %s your invitation to the event: %s",
                invitee.getFirstName(), invitee.getLastName(),
                response.toString().toLowerCase(),
                event.getTitle());
    }

    public String generateAddedAsOrganizerMessage(User invitor, Event event) {
        return String.format("%s %s added you as organizer to the event: %s",
                invitor.getFirstName(),
                invitor.getLastName(),
                event.getTitle());
    }

    public String generateRemovedAsOrganizerMessage(User invitor, Event event) {
        return String.format("%s %s removed you as organizer to the event: %s",
                invitor.getFirstName(),
                invitor.getLastName(),
                event.getTitle());
    }

}
