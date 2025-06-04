package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventSummaryDto;
import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.dtos.notificationDtos.NotificationResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.exceptions.ForbiddenOperationException;
import org.guram.eventscheduler.models.*;

import java.util.Set;
import java.util.stream.Collectors;

public class Utils {

    public static void checkIsOrganizer(Long actorUserId, Event event) {
        boolean actorIsOrganizer = event.getOrganizers().stream()
                .anyMatch(u -> u.getId().equals(actorUserId));
        if (!actorIsOrganizer) {
            throw new ForbiddenOperationException("User (ID=" + actorUserId + ") is not an organizer for this event.");
        }
    }

    public static EventResponseDto mapEventToResponseDto(Event event) {
        Set<UserSummaryDto> organizers = event.getOrganizers().stream()
                .map(org -> new UserSummaryDto(org.getId(), org.getFirstName(), org.getLastName(), org.getEmail()))
                .collect(Collectors.toSet());

        Set<Long> attendanceIds = event.getAttendances().stream()
                .map(Attendance::getId)
                .collect(Collectors.toSet());

        return new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                organizers,
                attendanceIds
        );
    }

    public static UserResponseDto mapUserToResponseDto(User user) {
        Set<Long> organizedEventIds = user.getOrganizedEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        Set<Long> attendanceIds = user.getAttendances().stream()
                .map(Attendance::getId)
                .collect(Collectors.toSet());

        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                organizedEventIds,
                attendanceIds
        );
    }

    public static AttendanceResponseDto mapAttendanceToResponseDto(Attendance attendance) {
        User user = attendance.getUser();
        UserSummaryDto userSummaryDto = new UserSummaryDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );

        Event event = attendance.getEvent();
        EventSummaryDto eventSummaryDto = new EventSummaryDto(
                event.getId(),
                event.getTitle(),
                event.getDateTime(),
                event.getLocation()
        );

        return new AttendanceResponseDto(
                attendance.getId(),
                userSummaryDto,
                eventSummaryDto,
                attendance.getStatus()
        );
    }

    public static InvitationResponseDto mapInvitationToResponseDto(Invitation invitation) {
        User invitee = invitation.getInvitee();
        UserSummaryDto inviteeSummary = new UserSummaryDto(
                invitee.getId(),
                invitee.getFirstName(),
                invitee.getLastName(),
                invitee.getEmail()
        );

        User invitor = invitation.getInvitor();
        UserSummaryDto invitorSummary = new UserSummaryDto(
                invitor.getId(),
                invitor.getFirstName(),
                invitor.getLastName(),
                invitor.getEmail()
        );

        Event event = invitation.getEvent();
        EventSummaryDto eventSummary = new EventSummaryDto(
                event.getId(),
                event.getTitle(),
                event.getDateTime(),
                event.getLocation()
        );

        return new InvitationResponseDto(
                invitation.getId(),
                inviteeSummary,
                invitorSummary,
                eventSummary,
                invitation.getInvitationSentDate(),
                invitation.getStatus()
        );
    }

    public static NotificationResponseDto mapNotificationToResponseDto(Notification notification) {
        User recipient = notification.getRecipient();
        UserSummaryDto recipientSummary = new UserSummaryDto(
                recipient.getId(),
                recipient.getFirstName(),
                recipient.getLastName(),
                recipient.getEmail()
        );

        return new NotificationResponseDto(
                notification.getId(),
                recipientSummary,
                notification.getMessage(),
                notification.getType(),
                notification.getCreatedAt()
        );
    }

}
