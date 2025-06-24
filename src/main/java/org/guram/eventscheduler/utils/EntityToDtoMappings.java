package org.guram.eventscheduler.utils;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventSummaryDto;
import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.dtos.notificationDtos.NotificationResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityToDtoMappings {

    public static EventResponseDto mapEventToResponseDto(Event event) {
        List<UserSummaryDto> attendees = new ArrayList<>();
        List<UserSummaryDto> organizers = new ArrayList<>();
        Map<Long, AttendanceStatus> userAttendanceStatusMap = new HashMap<>();

        for (Attendance att : event.getAttendances()) {
            if (att.getStatus() == AttendanceStatus.REGISTERED ||
                    att.getStatus() == AttendanceStatus.ATTENDED) {
                User user = att.getUser();
                UserSummaryDto userSummary = new UserSummaryDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getBio(),
                        user.getProfilePictureUrl()
                );

                if (att.getRole() == AttendanceRole.ATTENDEE)
                    attendees.add(userSummary);
                else if (att.getRole() == AttendanceRole.ORGANIZER)
                    organizers.add(userSummary);

                userAttendanceStatusMap.put(user.getId(), att.getStatus());
            }
        }


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
                user.getBio(),
                user.getProfilePictureUrl(),
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
                user.getEmail(),
                user.getBio(),
                user.getProfilePictureUrl()
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
                invitee.getEmail(),
                invitee.getBio(),
                invitee.getProfilePictureUrl()
        );

        User invitor = invitation.getInvitor();
        UserSummaryDto invitorSummary = new UserSummaryDto(
                invitor.getId(),
                invitor.getFirstName(),
                invitor.getLastName(),
                invitor.getEmail(),
                invitor.getBio(),
                invitor.getProfilePictureUrl()
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
                recipient.getEmail(),
                recipient.getBio(),
                recipient.getProfilePictureUrl()
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
