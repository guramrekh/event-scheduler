package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.DTOs.eventDTOs.EventCreateDto;
import org.guram.eventscheduler.DTOs.eventDTOs.EventEditDto;
import org.guram.eventscheduler.DTOs.eventDTOs.EventResponseDto;
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
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventCreateDto eventCreateDto) {
        EventResponseDto event = eventService.createEvent(eventCreateDto);
        return new ResponseEntity<>(event, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        EventResponseDto event = eventService.getEventById(id);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponseDto>> listUpcomingEvents() {
        List<EventResponseDto> upcomingEvents = eventService.findUpcomingEvents();
        return new ResponseEntity<>(upcomingEvents, HttpStatus.OK);
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<EventResponseDto> editEvent(
                        @PathVariable Long id,
                        @RequestParam Long actorUserId,
                        @Valid @RequestBody EventEditDto eventEditDto) {
        EventResponseDto event = eventService.editEvent(id, actorUserId, eventEditDto);
        return new ResponseEntity<>(event, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long id, @RequestParam Long actorUserId) {
        eventService.cancelEvent(id, actorUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/organizers")
    public ResponseEntity<EventResponseDto> addOrganizer(
                        @PathVariable Long id,
                        @RequestParam Long actorUserId,
                        @RequestParam Long newOrgUserId) {
        EventResponseDto event = eventService.addOrganizer(id, actorUserId, newOrgUserId);
        return new ResponseEntity<>(event, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}/organizers/{removeUserId}")
    public ResponseEntity<EventResponseDto> removeOrganizer(
                        @PathVariable Long id,
                        @PathVariable Long removeUserId,
                        @RequestParam Long actorUserId) {
        EventResponseDto event = eventService.removeOrganizer(id, actorUserId, removeUserId);
        return new ResponseEntity<>(event, HttpStatus.ACCEPTED);
    }
}
