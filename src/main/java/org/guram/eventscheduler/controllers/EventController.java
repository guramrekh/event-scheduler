package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.DTOs.EventCreateDto;
import org.guram.eventscheduler.DTOs.EventEditDto;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping("/create")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventCreateDto eventCreateDto) {
        Event event = eventService.createEvent(eventCreateDto);
        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.findEventById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> listUpcomingEvents() {
        List<Event> upcomingEvents = eventService.findUpcomingEvents();
        return new ResponseEntity<>(upcomingEvents, HttpStatus.OK);
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<Event> editEvent(
                        @PathVariable Long id,
                        @RequestParam Long actorUserId,
                        @Valid @RequestBody EventEditDto eventEditDto) {
        Event event = eventService.editEvent(id, actorUserId, eventEditDto);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long id, @RequestParam Long actorUserId) {
        eventService.cancelEvent(id, actorUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/organizers")
    public ResponseEntity<Event> addOrganizer(
                        @PathVariable Long id,
                        @RequestParam Long actorUserId,
                        @RequestParam Long newOrgUserId) {
        Event event = eventService.addOrganizer(id, actorUserId, newOrgUserId);
        return new ResponseEntity<>(event, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}/organizers/{removeUserId}")
    public ResponseEntity<Event> removeOrganizer(
                        @PathVariable Long id,
                        @PathVariable Long removeUserId,
                        @RequestParam Long actorUserId) {
        Event event = eventService.removeOrganizer(id, actorUserId, removeUserId);
        return new ResponseEntity<>(event, HttpStatus.ACCEPTED);
    }
}
