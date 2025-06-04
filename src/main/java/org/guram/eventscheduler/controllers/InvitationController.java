package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.DTOs.invitationDTOs.InvitationResponseDto;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.services.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return ResponseEntity.ok(invitation);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<InvitationResponseDto>> listAllInvitationsForEvent(@PathVariable Long eventId,
                                                @RequestParam(required = false) InvitationStatus status) {
        List<InvitationResponseDto> invitations;
        if (status != null)
            invitations = invitationService.listInvitationsForEventByStatus(eventId, status);
        else
            invitations = invitationService.listAllInvitationsForAnEvent(eventId);

        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/user/{userId}/sent")
    public ResponseEntity<List<InvitationResponseDto>> listInvitationsSentByUser(@PathVariable Long userId,
                                                        @RequestParam(required = false) InvitationStatus status) {
        List<InvitationResponseDto> invitations;
        if (status != null)
            invitations = invitationService.listInvitationsSentByUserByStatus(userId, status);
        else
            invitations = invitationService.listAllInvitationsSentByUser(userId);

        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/user/{userId}/received")
    public ResponseEntity<List<InvitationResponseDto>> listInvitationsReceivedByUser(@PathVariable Long userId,
                                                        @RequestParam(required = false) InvitationStatus status) {
        List<InvitationResponseDto> invitations;
        if (status != null)
            invitations = invitationService.listInvitationsReceivedByUserByStatus(userId, status);
        else
            invitations = invitationService.listAllInvitationsReceivedByUser(userId);

        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvitationResponseDto> getInvitationById(@PathVariable Long id) {
        InvitationResponseDto invitation = invitationService.getInvitationById(id);
        return ResponseEntity.ok(invitation);
    }

}
