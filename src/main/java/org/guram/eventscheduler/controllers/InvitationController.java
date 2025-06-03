package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.DTOs.invitationDTOs.InvitationResponseDto;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.services.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/invitation")
public class InvitationController {

    private final InvitationService invitationService;

    @Autowired
    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }


    @PostMapping("/invite")
    public ResponseEntity<InvitationResponseDto> inviteUser(@RequestParam Long eventId,
                                                            @RequestParam Long invitorId,
                                                            @RequestParam Long inviteeId) {
        InvitationResponseDto invitation = invitationService.sendInvitation(invitorId, inviteeId, eventId);
        URI location = URI.create("/invitation/" + invitation.id());
        return ResponseEntity.created(location).body(invitation);
    }

    @PutMapping("/respond")
    public ResponseEntity<InvitationResponseDto> respondToInvitation(@RequestParam Long invitationId,
                                                                     @RequestParam Long inviteeId,
                                                                     @RequestParam InvitationStatus newStatus) {
        InvitationResponseDto invitation = invitationService.respondToInvitation(invitationId, inviteeId, newStatus);
        return new ResponseEntity<>(invitation, HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<List<InvitationResponseDto>> listAllInvitationsForEvent(@PathVariable Long eventId,
                                                @RequestParam(required = false) InvitationStatus status) {
        List<InvitationResponseDto> invitations = invitationService.listInvitationsForEvent(eventId, status);
        return new ResponseEntity<>(invitations, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/sent")
    public ResponseEntity<List<InvitationResponseDto>> listInvitationsSentByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) InvitationStatus status) {
        List<InvitationResponseDto> invitations = invitationService.listInvitationsSentByUser(userId, status);
        return new ResponseEntity<>(invitations, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/received")
    public ResponseEntity<List<InvitationResponseDto>> listInvitationsReceivedByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) InvitationStatus status) {
        List<InvitationResponseDto> invitations = invitationService.listInvitationsReceivedByUser(userId, status);
        return new ResponseEntity<>(invitations, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvitationResponseDto> getInvitationById(@PathVariable Long id) {
        InvitationResponseDto invitation = invitationService.getInvitationById(id);
        return new ResponseEntity<>(invitation, HttpStatus.OK);
    }

}
