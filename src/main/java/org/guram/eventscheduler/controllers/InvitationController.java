package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.services.InvitationService;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;

    @Autowired
    public InvitationController(InvitationService invitationService, UserService userService) {
        this.invitationService = invitationService;
        this.userService = userService;
    }


    @PostMapping("/invite")
    public ResponseEntity<InvitationResponseDto> inviteUser(
                                            @RequestParam Long eventId,
                                            @RequestParam Long inviteeId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = userService.getCurrentUser(userDetails).getId();
        var invitation = invitationService.sendInvitation(currentUserId, inviteeId, eventId);
        URI location = URI.create("/invitations/" + invitation.id());
        return ResponseEntity.created(location).body(invitation);
    }

    @PutMapping("/{invitationId}/respond")
    public ResponseEntity<InvitationResponseDto> respondToInvitation(
                                            @PathVariable Long invitationId,
                                            @RequestParam InvitationStatus newStatus,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = userService.getCurrentUser(userDetails).getId();
        var invitation = invitationService.respondToInvitation(currentUserId, invitationId, newStatus);
        return ResponseEntity.ok(invitation);
    }

    @GetMapping()
    public ResponseEntity<List<InvitationResponseDto>> getInvitationsByStatus(
                                            @RequestParam(required = false) InvitationStatus status,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        var invitations = invitationService.listInvitationsReceivedByUserByStatus(currentUser, status);
        return ResponseEntity.ok(invitations);
    }

}
