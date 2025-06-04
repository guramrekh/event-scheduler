package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.services.InvitationService;
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
@RequestMapping("/invitation")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    @Autowired
    public InvitationController(InvitationService invitationService, UserService userService) {
        this.invitationService = invitationService;
        this.userService = userService;
    }


    @PostMapping("/invite")
    public ResponseEntity<InvitationResponseDto> inviteUser(@RequestParam Long eventId,
                                                    @RequestParam Long inviteeId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        InvitationResponseDto invitation = invitationService.sendInvitation(currentUserId, inviteeId, eventId);
        URI location = URI.create("/invitation/" + invitation.id());
        return ResponseEntity.created(location).body(invitation);
    }

    @PutMapping("/{invitationId}/respond")
    public ResponseEntity<InvitationResponseDto> respondToInvitation(@PathVariable Long invitationId,
                                                     @RequestParam InvitationStatus newStatus,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        InvitationResponseDto invitation = invitationService.respondToInvitation(
                invitationId, currentUserId, newStatus);
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

    @GetMapping("/sent")
    public ResponseEntity<List<InvitationResponseDto>> listInvitationsSentByUser(
                                            @AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam(required = false) InvitationStatus status) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        List<InvitationResponseDto> invitations;
        if (status != null)
            invitations = invitationService.listInvitationsSentByUserByStatus(currentUserId, status);
        else
            invitations = invitationService.listAllInvitationsSentByUser(currentUserId);

        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/received")
    public ResponseEntity<List<InvitationResponseDto>> listInvitationsReceivedByUser(
                                            @AuthenticationPrincipal UserDetails userDetails,
                                            @RequestParam(required = false) InvitationStatus status) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        List<InvitationResponseDto> invitations;
        if (status != null)
            invitations = invitationService.listInvitationsReceivedByUserByStatus(currentUserId, status);
        else
            invitations = invitationService.listAllInvitationsReceivedByUser(currentUserId);

        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvitationResponseDto> getInvitationById(@PathVariable Long id) {
        InvitationResponseDto invitation = invitationService.getInvitationById(id);
        return ResponseEntity.ok(invitation);
    }

}
