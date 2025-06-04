package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.exceptions.*;
import org.guram.eventscheduler.models.*;
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
    private final NotificationService notificationService;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             AttendanceService attendanceService, NotificationService notificationService) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendanceService = attendanceService;
        this.notificationService = notificationService;
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

        String message = notificationService.generateInvitationMessage(savedInvitation.getEvent());
        notificationService.createNotification(savedInvitation.getInvitee(), message, NotificationType.EVENT_INVITATION_RECEIVED);

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

        User invitor = invitation.getInvitor();
        String message = notificationService.generateInvitationResponseMessage(invitation.getInvitee(),
                invitation.getEvent(), response);

        if (response == InvitationStatus.ACCEPTED) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            attendanceService.registerUser(invitation.getInvitee(), invitation.getEvent());
            invitation.getEvent().getOrganizers().forEach(organizer ->
                    notificationService.createNotification(organizer, message, NotificationType.INVITATION_ACCEPTED));
        }
        else if (response == InvitationStatus.DECLINED) {
            invitation.setStatus(InvitationStatus.DECLINED);
            invitation.getEvent().getOrganizers().forEach(organizer ->
                    notificationService.createNotification(organizer, message, NotificationType.INVITATION_DECLINED));
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
        return Utils.mapInvitationToResponseDto(invitation);
    }

    public List<InvitationResponseDto> listInvitationsSentByUserByStatus(Long userId, InvitationStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return invitationRepository.findByInvitorAndStatus(user, status).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

    public List<InvitationResponseDto> listAllInvitationsSentByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return invitationRepository.findByInvitor(user).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

    public List<InvitationResponseDto> listInvitationsReceivedByUserByStatus(Long userId, InvitationStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return invitationRepository.findByInviteeAndStatus(user, status).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

    public List<InvitationResponseDto> listAllInvitationsReceivedByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return invitationRepository.findByInvitee(user).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

    public List<InvitationResponseDto> listInvitationsForEventByStatus(Long eventId, InvitationStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return invitationRepository.findByEventAndStatus(event, status).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

    public List<InvitationResponseDto> listAllInvitationsForAnEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return invitationRepository.findByEvent(event).stream()
                .map(Utils::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }
}
