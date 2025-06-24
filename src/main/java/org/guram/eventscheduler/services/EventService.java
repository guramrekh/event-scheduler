package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.eventDtos.EventRequestDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventWithRoleDto;
import org.guram.eventscheduler.exceptions.*;
import org.guram.eventscheduler.models.*;
import org.guram.eventscheduler.repositories.AttendanceRepository;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.guram.eventscheduler.utils.EntityToDtoMappings.mapEventToResponseDto;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final AttendanceRepository attendanceRepo;
    private final NotificationService notificationService;
    private final AttendanceService attendanceService;

    @Autowired
    public EventService(EventRepository eventRepo,
                        UserRepository userRepo,
                        AttendanceRepository attendanceRepo,
                        NotificationService notificationService,
                        AttendanceService attendanceService) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.attendanceRepo = attendanceRepo;
        this.notificationService = notificationService;
        this.attendanceService = attendanceService;
    }


    @Transactional
    public EventResponseDto createEvent(User organizer, EventRequestDto eventRequestDto) {
        Event event = new Event();
        event.setTitle(eventRequestDto.title());
        event.setDescription(eventRequestDto.description());
        event.setDateTime(eventRequestDto.dateTime());
        event.setLocation(eventRequestDto.location());

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);

        event.getAttendances().add(organizerAttendance);
        organizer.getAttendances().add(organizerAttendance);

        Event savedEvent = eventRepo.save(event);
        return mapEventToResponseDto(savedEvent);
    }

    @Transactional
    public EventResponseDto makeAttendeeOrganizer(User actorUser, Long newOrgUserId, Long eventId) {
        Event event = findEventById(eventId);

        checkIsOrganizer(actorUser.getId(), event);

        User newOrgUser = userRepo.findById(newOrgUserId)
                .orElseThrow(() -> new UserNotFoundException(newOrgUserId));

        Attendance newOrganizerAttendance = attendanceRepo.findByUserAndEvent(newOrgUser, event)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", newOrgUserId));

        if (newOrganizerAttendance.getRole() == AttendanceRole.ORGANIZER)
            return mapEventToResponseDto(event);

        newOrganizerAttendance.setRole(AttendanceRole.ORGANIZER);

        String message = notificationService.generateAddedAsOrganizerMessage(actorUser, event);
        notificationService.createNotification(newOrgUser, message, NotificationType.ADDED_AS_ORGANIZER);

        return mapEventToResponseDto(event);
    }

    @Transactional
    public EventResponseDto removeOrganizerRole(User actorUser, Long removeUserId, Long eventId) {
        Event event = findEventById(eventId);

        checkIsOrganizer(actorUser.getId(), event);

        User orgToRemove = userRepo.findById(removeUserId)
                .orElseThrow(() -> new UserNotFoundException(removeUserId));

        checkIsOrganizer(removeUserId, event);

        Attendance removeOrgAttendance = attendanceRepo.findByUserAndEvent(orgToRemove, event)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", removeUserId));

        if (removeOrgAttendance.getRole() == AttendanceRole.ATTENDEE)
            return mapEventToResponseDto(event);

        removeOrgAttendance.setRole(AttendanceRole.ATTENDEE);

        String message = notificationService.generateRemovedAsOrganizerMessage(actorUser, event);
        notificationService.createNotification(orgToRemove, message, NotificationType.REMOVED_AS_ORGANIZER);

        return mapEventToResponseDto(event);
    }

    @Transactional
    public EventResponseDto kickUserFromEvent(User organizer, Long userToKickId, Long eventId) {
        Event event = findEventById(eventId);

        checkIsOrganizer(organizer.getId(), event);

        User userToKick = userRepo.findById(userToKickId)
                .orElseThrow(() -> new UserNotFoundException(userToKickId));

        Attendance kickUserAttendance = attendanceRepo.findByUserAndEvent(userToKick, event)
                .orElseThrow(() -> new ResourceNotFoundException("User (ID=" + userToKickId + ") is not attending this event (ID=" + eventId + ")."));

        kickUserAttendance.setStatus(AttendanceStatus.KICKED);

        String message = notificationService.generateKickedOutFromEventMessage(organizer, event);
        notificationService.createNotification(userToKick, message, NotificationType.REMOVED_AS_ORGANIZER);

        return mapEventToResponseDto(event);
    }

    @Transactional
    public EventResponseDto editEvent(Long eventId, Long actorUserId,
                                      EventRequestDto eventRequestDto, boolean notifyParticipants) {
        Event event = findEventById(eventId);

        checkIsOrganizer(actorUserId, event);

        event.setTitle(eventRequestDto.title());
        event.setDescription(eventRequestDto.description());
        event.setDateTime(eventRequestDto.dateTime());
        event.setLocation(eventRequestDto.location());

        Event editedEvent = eventRepo.save(event);

        if (notifyParticipants) {
            String message = notificationService.generateEventUpdatedMessage(editedEvent);
            attendanceRepo.findByEventAndStatusOrderByEvent_DateTimeAsc(editedEvent, AttendanceStatus.REGISTERED).stream()
                    .map(Attendance::getUser)
                    .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_DETAILS_UPDATED));

            editedEvent.getInvitations().stream()
                    .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                    .map(Invitation::getInvitee)
                    .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_DETAILS_UPDATED));
        }

        return mapEventToResponseDto(editedEvent);
    }

    @Transactional
    public void cancelEvent(Long eventId, Long actorUserId) {
        Event event = findEventById(eventId);

        checkIsOrganizer(actorUserId, event);

        event.setCancelled(true);

        String message = notificationService.generateEventCancelledMessage(event);
        attendanceRepo.findByEventAndStatusOrderByEvent_DateTimeAsc(event, AttendanceStatus.REGISTERED).stream()
                .map(Attendance::getUser)
                .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_CANCELLED));

        event.getInvitations().stream()
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                .map(Invitation::getInvitee)
                .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_CANCELLED));

        eventRepo.save(event);
    }

    public List<EventWithRoleDto> getFilteredEventsWithRole(User user, AttendanceRole role,
                                                            String timeframe, boolean cancelled) {
        if (!timeframe.equals("UPCOMING") && !timeframe.equals("PAST"))
            throw new IllegalArgumentException("Timeframe must be either UPCOMING or PAST.");

        boolean upcoming = timeframe.equals("UPCOMING");
        return eventRepo.findByUserAndRoleAndDateTimeAndStatus(user, role, upcoming, cancelled).stream()
                .map(event -> new EventWithRoleDto(
                        mapEventToResponseDto(event),
                        attendanceService.getAttendanceRole(user, event))
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponseDto markAttended(User organizer, Long attendeeUserId, Long eventId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        checkIsOrganizer(organizer.getId(), event);

        User attendee = userRepo.findById(attendeeUserId)
                .orElseThrow(() -> new UserNotFoundException(attendeeUserId));

        Attendance attendance = attendanceRepo.findByUserAndEvent(attendee, event)
                .orElseThrow(() -> new ConflictException("No attendance record for this user/event."));

        if (attendance.getStatus() != AttendanceStatus.REGISTERED) {
            throw new InvalidStatusTransitionException(
                    "Cannot mark attendance because current status is " + attendance.getStatus());
        }

        attendance.setStatus(AttendanceStatus.ATTENDED);
        return mapEventToResponseDto(event);
    }

    @Transactional
    public EventResponseDto markAllAttended(User organizer, Long eventId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        checkIsOrganizer(organizer.getId(), event);

        attendanceRepo.markAllAsAttended(event, AttendanceStatus.REGISTERED, AttendanceStatus.ATTENDED);

        return mapEventToResponseDto(event);
    }

    private Event findEventById(Long eventId) {
        return eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    public static void checkIsOrganizer(Long actorUserId, Event event) {
        boolean actorIsOrganizer = event.getAttendances().stream()
                .filter(att -> att.getRole() == AttendanceRole.ORGANIZER)
                .anyMatch(att -> att.getUser().getId().equals(actorUserId));

        if (!actorIsOrganizer)
            throw new ForbiddenOperationException("User (ID=" + actorUserId + ") is not an organizer for this event.");
    }

}
