package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.exceptions.*;
import org.guram.eventscheduler.models.*;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.InvitationRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.guram.eventscheduler.utils.EntityToDtoMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.guram.eventscheduler.services.EventService.checkIsOrganizer;
import static org.guram.eventscheduler.utils.EntityToDtoMappings.mapInvitationToResponseDto;

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
                             AttendanceService attendanceService,
                             NotificationService notificationService) {
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

        checkIsOrganizer(invitorId, event);

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

        return mapInvitationToResponseDto(savedInvitation);
    }

    @Transactional
    public InvitationResponseDto respondToInvitation(Long inviteeId, Long invitationId,
                                                     InvitationStatus response) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", invitationId));

        if (!invitation.getInvitee().getId().equals(inviteeId)) {
            throw new ForbiddenOperationException("User (ID=" + inviteeId + ") is not authorized to respond to this invitation.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot respond to invitation with current status: " + invitation.getStatus() + ". Only PENDING invitations can be responded to.");
        }

        String message = notificationService.generateInvitationResponseMessage(
                invitation.getInvitee(), invitation.getEvent(), response);

        if (response == InvitationStatus.ACCEPTED) {
            invitation.setStatus(InvitationStatus.ACCEPTED);
            attendanceService.registerUser(invitation.getInvitee(), invitation.getEvent());
            invitation.getEvent().getAttendances().stream()
                    .filter(attendance -> attendance.getRole() == AttendanceRole.ORGANIZER)
                    .map(Attendance::getUser)
                    .forEach(organizer -> notificationService.createNotification(
                            organizer, message, NotificationType.INVITATION_ACCEPTED));
        }
        else if (response == InvitationStatus.DECLINED) {
            invitation.setStatus(InvitationStatus.DECLINED);
            invitation.getEvent().getAttendances().stream()
                    .filter(attendance -> attendance.getRole() == AttendanceRole.ORGANIZER)
                    .map(Attendance::getUser)
                    .forEach(organizer -> notificationService.createNotification(
                            organizer, message, NotificationType.INVITATION_DECLINED));
        }
        else {
            throw new InvalidStatusTransitionException("Invalid response type. Must be ACCEPTED or DECLINED.");
        }

        Invitation updatedInvitation = invitationRepository.save(invitation);
        return mapInvitationToResponseDto(updatedInvitation);
    }

    public List<InvitationResponseDto> listInvitationsReceivedByUserByStatus(User user, InvitationStatus status) {
        status = (status == null) ? InvitationStatus.PENDING : status;
        return invitationRepository.findByInviteeAndStatusOrderByInvitationSentDateAsc(user, status).stream()
                .map(EntityToDtoMappings::mapInvitationToResponseDto)
                .collect(Collectors.toList());
    }

}
