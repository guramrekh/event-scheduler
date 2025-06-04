package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.dtos.eventDtos.EventCreateDto;
import org.guram.eventscheduler.dtos.eventDtos.EventEditDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.services.EventService;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.guram.eventscheduler.controllers.Utils.getCurrentUser;

@RestController
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @Autowired
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }


    @PostMapping("/create")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventCreateDto eventCreateDto) {
        EventResponseDto event = eventService.createEvent(eventCreateDto);
        URI location = URI.create("/event/" + event.id());
        return ResponseEntity.created(location).body(event);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        EventResponseDto event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponseDto>> listUpcomingEvents() {
        List<EventResponseDto> upcomingEvents = eventService.findUpcomingEvents();
        return ResponseEntity.ok(upcomingEvents);
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<EventResponseDto> editEvent(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody EventEditDto eventEditDto) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        EventResponseDto event = eventService.editEvent(id, currentUserId, eventEditDto);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancelEvent(@PathVariable Long eventId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        eventService.cancelEvent(eventId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/organizers/add")
    public ResponseEntity<EventResponseDto> addOrganizer(
                                    @PathVariable Long eventId,
                                    @RequestParam Long newOrgUserId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserID = getCurrentUser(userDetails, userService).getId();
        EventResponseDto event = eventService.addOrganizer(eventId, currentUserID, newOrgUserId);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{eventId}/organizers/remove")
    public ResponseEntity<EventResponseDto> removeOrganizer(
                                    @PathVariable Long eventId,
                                    @RequestParam Long removeUserId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        EventResponseDto event = eventService.removeOrganizer(eventId, currentUserId, removeUserId);
        return ResponseEntity.ok(event);
    }
}
