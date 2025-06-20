package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.eventDtos.EventCreateDto;
import org.guram.eventscheduler.dtos.eventDtos.EventEditDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.exceptions.EventNotFoundException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.*;
import org.guram.eventscheduler.repositories.AttendanceRepository;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final AttendanceRepository attendanceRepo;
    private final NotificationService notificationService;

    @Autowired
    public EventService(EventRepository eventRepo, UserRepository userRepo, AttendanceRepository attendanceRepo, NotificationService notificationService) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.attendanceRepo = attendanceRepo;
        this.notificationService = notificationService;
    }


    @Transactional
    public EventResponseDto createEvent(EventCreateDto eventCreateDto) {
        User organizer = userRepo.findById(eventCreateDto.organizerUserId())
                .orElseThrow(() -> new UserNotFoundException(eventCreateDto.organizerUserId()));

        Event event = new Event();
        event.setTitle(eventCreateDto.title());
        event.setDescription(eventCreateDto.description());
        event.setDateTime(eventCreateDto.dateTime());
        event.setLocation(eventCreateDto.location());

        event.getOrganizers().add(organizer);
        organizer.getOrganizedEvents().add(event);

        Event savedEvent = eventRepo.save(event);
        return Utils.mapEventToResponseDto(savedEvent);
    }

    @Transactional
    public EventResponseDto addOrganizer(Long eventId, Long actorUserId, Long newOrgUserId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Utils.checkIsOrganizer(actorUserId, event);

        User toAdd = userRepo.findById(newOrgUserId)
                .orElseThrow(() -> new UserNotFoundException(newOrgUserId));

        boolean alreadyOrganizer = event.getOrganizers().stream()
                .anyMatch(u -> u.getId().equals(newOrgUserId));
        if (alreadyOrganizer) {
            return Utils.mapEventToResponseDto(event);
        }

        event.getOrganizers().add(toAdd);
        toAdd.getOrganizedEvents().add(event);

        Event updatedEvent = eventRepo.save(event);

        User actor = userRepo.findById(actorUserId).orElseThrow(() -> new UserNotFoundException(actorUserId));
        String message = notificationService.generateAddedAsOrganizerMessage(actor, event);
        notificationService.createNotification(toAdd, message, NotificationType.ADDED_AS_ORGANIZER);

        return Utils.mapEventToResponseDto(updatedEvent);
    }

    @Transactional
    public EventResponseDto removeOrganizer(Long eventId, Long actorUserId, Long removeUserId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Utils.checkIsOrganizer(actorUserId, event);

        User toRemove = userRepo.findById(removeUserId)
                .orElseThrow(() -> new UserNotFoundException(removeUserId));

        Utils.checkIsOrganizer(removeUserId, event);

        event.getOrganizers().remove(toRemove);
        toRemove.getOrganizedEvents().remove(event);

        Event updatedEvent = eventRepo.save(event);

        User actor = userRepo.findById(actorUserId).orElseThrow(() -> new UserNotFoundException(actorUserId));
        String message = notificationService.generateRemovedAsOrganizerMessage(actor, event);
        notificationService.createNotification(toRemove, message, NotificationType.REMOVED_AS_ORGANIZER);

        return Utils.mapEventToResponseDto(updatedEvent);
    }

    @Transactional
    public EventResponseDto editEvent(Long eventId, Long actorUserId, EventEditDto eventEditDto) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Utils.checkIsOrganizer(actorUserId, event);

        if (eventEditDto.title() != null)
            event.setTitle(eventEditDto.title());
        if (eventEditDto.description() != null)
            event.setDescription(eventEditDto.description());
        if (eventEditDto.dateTime() != null)
            event.setDateTime(eventEditDto.dateTime());
        if (eventEditDto.location() != null)
            event.setLocation(eventEditDto.location());

        Event editedEvent = eventRepo.save(event);

        String message = notificationService.generateEventUpdatedMessage(editedEvent);
        attendanceRepo.findByEventAndStatus(editedEvent, AttendanceStatus.REGISTERED).stream()
                .map(Attendance::getUser)
                .distinct()
                .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_DETAILS_UPDATED));

        editedEvent.getInvitations().stream()
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                .map(Invitation::getInvitee)
                .distinct()
                .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_DETAILS_UPDATED));

        return Utils.mapEventToResponseDto(editedEvent);
    }

    @Transactional
    public void cancelEvent(Long eventId, Long actorUserId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Utils.checkIsOrganizer(actorUserId, event);

        event.getOrganizers().forEach(organizer -> organizer.getOrganizedEvents().remove(event));
        event.getOrganizers().clear();

        String message = notificationService.generateEventCancelledMessage(event);
        attendanceRepo.findByEventAndStatus(event, AttendanceStatus.REGISTERED).stream()
                .map(Attendance::getUser)
                .distinct()
                .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_CANCELLED));

        event.getInvitations().stream()
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                .map(Invitation::getInvitee)
                .distinct()
                .forEach(user -> notificationService.createNotification(user, message, NotificationType.EVENT_CANCELLED));

        eventRepo.delete(event);
    }

    public EventResponseDto getEventById(Long eventId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return Utils.mapEventToResponseDto(event);
    }

    public List<EventResponseDto> findUpcomingEvents() {
        return eventRepo.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now()).stream()
                .map(Utils::mapEventToResponseDto)
                .collect(Collectors.toList());
    }

    private Optional<Event> findEventById(Long eventId) {
        return eventRepo.findById(eventId);
    }

}
