package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.dtos.eventDtos.EventRequestDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventWithRoleDto;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.services.EventService;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @Autowired
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }


    @GetMapping()
    public ResponseEntity<List<EventWithRoleDto>> getEvents(
                                    @RequestParam(defaultValue = "UPCOMING") String timeframe,
                                    @RequestParam(required = false) AttendanceRole role,
                                    @RequestParam(defaultValue = "false") boolean showCancelled,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        var events = eventService.getFilteredEventsWithRole(currentUser, role, timeframe, showCancelled);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/create")
    public ResponseEntity<EventResponseDto> createEvent(
                                    @Valid @RequestBody EventRequestDto eventRequestDto,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        EventResponseDto event = eventService.createEvent(currentUser, eventRequestDto);
        URI location = URI.create("/events/" + event.id());
        return ResponseEntity.created(location).body(event);
    }

    @PutMapping("/{eventId}/edit")
    public ResponseEntity<EventResponseDto> editEvent(
                                    @PathVariable Long eventId,
                                    @RequestParam(defaultValue = "true") boolean notifyParticipants,
                                    @Valid @RequestBody EventRequestDto eventRequestDto,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = userService.getCurrentUser(userDetails).getId();
        EventResponseDto event = eventService.editEvent(eventId, currentUserId, eventRequestDto, notifyParticipants);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancelEvent(
                                    @PathVariable Long eventId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = userService.getCurrentUser(userDetails).getId();
        eventService.cancelEvent(eventId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{eventId}/add-organizer")
    public ResponseEntity<EventResponseDto> addAsOrganizer(
                                    @PathVariable Long eventId,
                                    @RequestParam Long newOrgUserId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        EventResponseDto event = eventService.makeAttendeeOrganizer(currentUser, newOrgUserId, eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}/remove-organizer")
    public ResponseEntity<EventResponseDto> removeAsOrganizer(
                                    @PathVariable Long eventId,
                                    @RequestParam Long removeUserId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        EventResponseDto event = eventService.removeOrganizerRole(currentUser, removeUserId, eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}/kickout-attendee")
    public ResponseEntity<EventResponseDto> kickOutAttendee(
                                    @PathVariable Long eventId,
                                    @RequestParam Long removeUserId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        EventResponseDto event = eventService.kickUserFromEvent(currentUser, removeUserId, eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/mark-attended")
    public ResponseEntity<EventResponseDto> markAttended(
                                    @RequestParam Long eventId,
                                    @RequestParam Long attendeeUserId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        EventResponseDto event = eventService.markAttended(currentUser, attendeeUserId, eventId);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/mark-all-attended")
    public ResponseEntity<EventResponseDto> markAllAttended(
                                    @RequestParam Long eventId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        EventResponseDto event = eventService.markAllAttended(currentUser, eventId);
        return ResponseEntity.ok(event);
    }

}
