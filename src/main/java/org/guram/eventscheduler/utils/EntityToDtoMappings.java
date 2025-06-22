package org.guram.eventscheduler.utils;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventSummaryDto;
import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.dtos.notificationDtos.NotificationResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityToDtoMappings {

    public static EventResponseDto mapEventToResponseDto(Event event) {
        var everyAttendance = event.getAttendances();

        List<UserSummaryDto> attendees = everyAttendance.stream()
                .filter(att -> att.getRole() == AttendanceRole.ATTENDEE &&
                        (att.getStatus() == AttendanceStatus.REGISTERED || att.getStatus() == AttendanceStatus.ATTENDED))
                .map(Attendance::getUser)
                .map(user -> new UserSummaryDto(
                        user.getId(), user.getFirstName(), user.getLastName(), user.getEmail()
                ))
                .toList();

        List<UserSummaryDto> organizers = everyAttendance.stream()
                .filter(att -> att.getRole() == AttendanceRole.ORGANIZER &&
                        (att.getStatus() == AttendanceStatus.REGISTERED || att.getStatus() == AttendanceStatus.ATTENDED))
                .map(Attendance::getUser)
                .map(user -> new UserSummaryDto(
                        user.getId(), user.getFirstName(), user.getLastName(), user.getEmail()
                ))
                .toList();

        Map<Long, AttendanceStatus> userAttendanceStatusMap = everyAttendance.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.REGISTERED ||
                        att.getStatus() == AttendanceStatus.ATTENDED)
                .collect(Collectors.toMap(att -> att.getUser().getId(), Attendance::getStatus));

        return new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                event.isCancelled(),
                attendees,
                organizers,
                userAttendanceStatusMap
        );
    }

    public static UserResponseDto mapUserToResponseDto(User user) {
        var everyAttendance = user.getAttendances();
        long attendedEventsCount = everyAttendance.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.ATTENDED)
                .count();

        long organizedEventsCount = everyAttendance.stream()
                .filter(att ->
                        att.getStatus() == AttendanceStatus.ATTENDED &&
                        att.getRole() == AttendanceRole.ORGANIZER
                )
                .count();

        long withdrawnFromEventsCount = everyAttendance.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.WITHDRAWN)
                .count();

        long kickedOutFromEventsCount = everyAttendance.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.KICKED)
                .count();

        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                (int) attendedEventsCount,
                (int) organizedEventsCount,
                (int) withdrawnFromEventsCount,
                (int) kickedOutFromEventsCount
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
                event.getDescription(),
                event.getDateTime(),
                event.getLocation(),
                event.isCancelled()
        );

        return new AttendanceResponseDto(
                attendance.getId(),
                userSummaryDto,
                eventSummaryDto,
                attendance.getStatus(),
                attendance.getRole()
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

        EventResponseDto eventResponseDto = mapEventToResponseDto(invitation.getEvent());

        return new InvitationResponseDto(
                invitation.getId(),
                inviteeSummary,
                invitorSummary,
                eventResponseDto,
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
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
