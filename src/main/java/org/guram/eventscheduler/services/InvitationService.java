package org.guram.eventscheduler.services;

import org.guram.eventscheduler.DTOs.invitationDTOs.InvitationResponseDto;
import org.guram.eventscheduler.exceptions.*;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.Invitation;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.InvitationRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AttendanceService attendanceService;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             AttendanceService attendanceService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendanceService = attendanceService;
    }


    @Transactional
    public InvitationResponseDto sendInvitation(Long invitorId, Long inviteeId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        User invitor = userRepository.findById(invitorId)
                .orElseThrow(() -> new UserNotFoundException(invitorId));

        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new UserNotFoundException(inviteeId));

        Utils.checkIsOrganizer(invitorId, event);

        Optional<Invitation> existingInvitation = invitationRepository.findByInviteeAndEvent(invitee, event);
        if (existingInvitation.isPresent()) {
            throw new ConflictException("An invitation for user (ID=" + inviteeId + ") to event (ID=" + eventId + ") " +
                    "already exists with status " + existingInvitation.get().getStatus() + ".");
        }

        Invitation newInvitation = new Invitation();
        newInvitation.setInvitor(invitor);
        newInvitation.setInvitee(invitee);
        newInvitation.setEvent(event);

        invitee.getReceivedInvitations().add(newInvitation);
        invitor.getSentInvitations().add(newInvitation);
        event.getInvitations().add(newInvitation);

        Invitation savedInvitation = invitationRepository.save(newInvitation);
        return Utils.mapInvitationToResponseDto(savedInvitation);
    }

    @Transactional
    public InvitationResponseDto respondToInvitation(Long invitationId, Long inviteeId, InvitationStatus response) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", invitationId));

        if (!invitation.getInvitee().getId().equals(inviteeId)) {
            throw new ForbiddenOperationException("User (ID=" + inviteeId + ") is not authorized to respond to this invitation.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot respond to invitation with current status: " + invitation.getStatus() + ". Only PENDING invitations can be responded to.");
        }

        if (response == InvitationStatus.ACCEPTED) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            attendanceService.registerUser(invitation.getInvitee(), invitation.getEvent());
        }
        else if (response == InvitationStatus.DECLINED) {
            invitation.setStatus(InvitationStatus.DECLINED);
        }
        else {
            throw new InvalidStatusTransitionException("Invalid response type. Must be ACCEPTED or DECLINED.");
        }

        Invitation updatedInvitation = invitationRepository.save(invitation);
        return Utils.mapInvitationToResponseDto(updatedInvitation);
    }

    public InvitationResponseDto getInvitationById(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", invitationId));
        // Add authorization here if needed (e.g., only invitor/invitee/organizer can view)
        return Utils.mapInvitationToResponseDto(invitation);
    }

    public List<InvitationResponseDto> listInvitationsSentByUser(Long userId, InvitationStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return invitationRepository.findByInvitorAndStatus(user, status).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

    public List<InvitationResponseDto> listInvitationsReceivedByUser(Long userId, InvitationStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return invitationRepository.findByInviteeAndStatus(user, status).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

    public List<InvitationResponseDto> listInvitationsForEvent(Long eventId, InvitationStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return invitationRepository.findByEventAndStatus(event, status).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }
}
