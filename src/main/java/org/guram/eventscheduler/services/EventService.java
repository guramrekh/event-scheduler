package org.guram.eventscheduler.services;

import org.guram.eventscheduler.DTOs.EventCreateDto;
import org.guram.eventscheduler.DTOs.EventEditDto;
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
    public Event createEvent(EventCreateDto eventCreateDto) {
        User organizer = userRepo.findById(eventCreateDto.organizerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found (ID=" + eventCreateDto.organizerUserId() + ")"));

        Event event = new Event();
        event.setTitle(eventCreateDto.title());
        event.setDescription(eventCreateDto.description());
        event.setDateTime(eventCreateDto.dateTime());
        event.setLocation(eventCreateDto.location());

        event.getOrganizers().add(organizer);
        organizer.getOrganizedEvents().add(event);

        return eventRepo.save(event);
    }

    @Transactional
    public Event addOrganizer(Long eventId, Long actorUserId, Long newOrgUserId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        Utils.checkIsOrganizer(actorUserId, event);

        User toAdd = userRepo.findById(newOrgUserId)
                .orElseThrow(() -> new IllegalArgumentException("User to add not found (ID=" + newOrgUserId + ")"));

        boolean alreadyOrganizer = event.getOrganizers().stream()
                .anyMatch(u -> u.getId().equals(newOrgUserId));
        if (alreadyOrganizer) {
            return event;
        }

        event.getOrganizers().add(toAdd);
        toAdd.getOrganizedEvents().add(event);

        return eventRepo.save(event);
    }

    @Transactional
    public Event removeOrganizer(Long eventId, Long actorUserId, Long removeUserId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        Utils.checkIsOrganizer(actorUserId, event);

        User toRemove = userRepo.findById(removeUserId)
                .orElseThrow(() -> new IllegalArgumentException("User to remove not found (ID=" + removeUserId + ")"));

        Utils.checkIsOrganizer(removeUserId, event);

        if (event.getOrganizers().size() <= 1)
            throw new IllegalStateException("Cannot remove only organizer from event (ID=" + eventId + ").");

        event.getOrganizers().remove(toRemove);
        toRemove.getOrganizedEvents().remove(event);

        return eventRepo.save(event);
    }

    @Transactional
    public Event editEvent(Long eventId, Long actorUserId, EventEditDto eventEditDto) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        Utils.checkIsOrganizer(actorUserId, event);

        if (eventEditDto.title() != null)
            event.setTitle(eventEditDto.title());
        if (eventEditDto.description() != null)
            event.setDescription(eventEditDto.description());
        if (eventEditDto.dateTime() != null)
            event.setDateTime(eventEditDto.dateTime());
        if (eventEditDto.location() != null)
            event.setLocation(eventEditDto.location());

        return eventRepo.save(event);
    }

    public void cancelEvent(Long eventId, Long actorUserId) {
        Event event = findEventById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        Utils.checkIsOrganizer(actorUserId, event);

        eventRepo.delete(event);
    }

    public Optional<Event> findEventById(Long eventId) {
        return eventRepo.findById(eventId);
    }

    public List<Event> findUpcomingEvents() {
        return eventRepo.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now());
    }

}
