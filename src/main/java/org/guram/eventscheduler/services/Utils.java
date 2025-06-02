package org.guram.eventscheduler.services;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.DTOs.attendanceDTOs.AttendanceResponseDto;
import org.guram.eventscheduler.DTOs.eventDTOs.EventResponseDto;
import org.guram.eventscheduler.DTOs.eventDTOs.EventSummaryDto;
import org.guram.eventscheduler.DTOs.userDTOs.OrganizerDto;
import org.guram.eventscheduler.DTOs.userDTOs.UserResponseDto;
import org.guram.eventscheduler.DTOs.userDTOs.UserSummaryDto;
import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;

import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    public static void checkIsOrganizer(Long actorUserId, Event event) {
        boolean actorIsOrganizer = event.getOrganizers().stream()
                .anyMatch(u -> u.getId().equals(actorUserId));
        if (!actorIsOrganizer) {
            throw new IllegalStateException("User (ID=" + actorUserId + ") is not an organizer for this event.");
        }
    }

    public static EventResponseDto mapEventToResponseDto(Event event) {
        Set<OrganizerDto> organizerDtos = event.getOrganizers().stream()
                .map(org -> new OrganizerDto(org.getId(), org.getFirstName(), org.getLastName(), org.getEmail()))
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
                organizerDtos,
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
        UserSummaryDto userSummaryDto = new UserSummaryDto(
                attendance.getUser().getId(),
                attendance.getUser().getFirstName(),
                attendance.getUser().getLastName(),
                attendance.getUser().getEmail()
        );
        EventSummaryDto eventSummaryDto = new EventSummaryDto(
                attendance.getEvent().getId(),
                attendance.getEvent().getTitle(),
                attendance.getEvent().getDateTime(),
                attendance.getEvent().getLocation()
        );

        return new AttendanceResponseDto(
                attendance.getId(),
                userSummaryDto,
                eventSummaryDto,
                attendance.getStatus()
        );
    }



}
