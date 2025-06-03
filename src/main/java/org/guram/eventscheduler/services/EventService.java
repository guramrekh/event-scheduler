package org.guram.eventscheduler.services;

import org.guram.eventscheduler.DTOs.eventDTOs.EventCreateDto;
import org.guram.eventscheduler.DTOs.eventDTOs.EventEditDto;
import org.guram.eventscheduler.DTOs.eventDTOs.EventResponseDto;
import org.guram.eventscheduler.exceptions.EventNotFoundException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
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

    @Autowired
    public EventService(EventRepository eventRepo, UserRepository userRepo) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
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
        return Utils.mapEventToResponseDto(editedEvent);
    }

    @Transactional
    public void cancelEvent(Long eventId, Long actorUserId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Utils.checkIsOrganizer(actorUserId, event);

        event.getOrganizers().forEach(organizer -> organizer.getOrganizedEvents().remove(event));
        event.getOrganizers().clear();

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
