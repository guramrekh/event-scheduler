package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.EventNotFoundException;
import org.guram.eventscheduler.exceptions.ForbiddenOperationException;
import org.guram.eventscheduler.exceptions.InvalidStatusTransitionException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.Invitation;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.models.NotificationType;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.InvitationRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InvitationService invitationService;


    @Test
    void sendInvitation_shouldSucceed_whenValidInput() {
        Event event = new Event();
        event.setId(1L);
        User invitor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        invitor.setId(1L);
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        invitee.setId(2L);

        Attendance organizerAttendance = new Attendance(invitor, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(invitor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(invitee));
        when(invitationRepository.findByInviteeAndEvent(invitee, event)).thenReturn(Optional.empty());
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvitationResponseDto result = invitationService.sendInvitation(1L, 2L, 1L);

        assertThat(result).isNotNull();
        ArgumentCaptor<Invitation> invitationArgumentCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invitationArgumentCaptor.capture());
        
        Invitation savedInvitation = invitationArgumentCaptor.getValue();
        assertThat(savedInvitation.getInvitee()).isEqualTo(invitee);
        assertThat(savedInvitation.getInvitor()).isEqualTo(invitor);
        assertThat(savedInvitation.getEvent()).isEqualTo(event);
        verify(notificationService).createNotification(invitee, notificationService.generateInvitationMessage(event), NotificationType.EVENT_INVITATION_RECEIVED);
    }

    @Test
    void sendInvitation_shouldThrowException_whenEventNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(EventNotFoundException.class, () -> invitationService.sendInvitation(1L, 2L, 1L));
    }

    @Test
    void sendInvitation_shouldThrowException_whenInvitorNotFound() {
        Event event = new Event();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(UserNotFoundException.class, () -> invitationService.sendInvitation(1L, 2L, 1L));
    }

    @Test
    void sendInvitation_shouldThrowException_whenInviteeNotFound() {
        Event event = new Event();
        User invitor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(invitor));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        
        assertThrows(UserNotFoundException.class, () -> invitationService.sendInvitation(1L, 2L, 1L));
    }

    @Test
    void sendInvitation_shouldThrowException_whenInvitorIsNotOrganizer() {
        Event event = new Event();
        event.setId(1L);
        User invitor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(invitor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(invitee));
        
        assertThrows(ForbiddenOperationException.class, () -> invitationService.sendInvitation(1L, 2L, 1L));
    }

    @Test
    void sendInvitation_shouldThrowException_whenDuplicateInvitationExists() {
        Event event = new Event();
        event.setId(1L);
        User invitor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        invitor.setId(1L);
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        invitee.setId(2L);

        Attendance organizerAttendance = new Attendance(invitor, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);
        Invitation existingInvitation = new Invitation();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(invitor));
        when(userRepository.findById(2L)).thenReturn(Optional.of(invitee));
        when(invitationRepository.findByInviteeAndEvent(invitee, event)).thenReturn(Optional.of(existingInvitation));

        assertThrows(ConflictException.class, () -> invitationService.sendInvitation(1L, 2L, 1L));
        
        verify(invitationRepository, never()).save(any(Invitation.class));
    }

    @Test
    void respondToInvitation_shouldSucceed_whenAcceptingInvitation() {
        Event event = new Event();
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        invitee.setId(1L);
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(2L);
        
        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        Invitation invitation = new Invitation();
        invitation.setId(1L);
        invitation.setEvent(event);
        invitation.setInvitee(invitee);
        invitation.setInvitor(organizer);
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvitationResponseDto result = invitationService.respondToInvitation(1L, 1L, InvitationStatus.ACCEPTED);
        
        assertThat(result).isNotNull();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(attendanceService).registerUser(invitee, event);
        verify(notificationService).createNotification(organizer, notificationService.generateInvitationResponseMessage(invitee, event, InvitationStatus.ACCEPTED), NotificationType.INVITATION_ACCEPTED);
    }

    @Test
    void respondToInvitation_shouldSucceed_whenDecliningInvitation() {
        Event event = new Event();
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        invitee.setId(1L);
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(2L);
        
        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        Invitation invitation = new Invitation();
        invitation.setId(1L);
        invitation.setEvent(event);
        invitation.setInvitee(invitee);
        invitation.setInvitor(organizer);
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvitationResponseDto result = invitationService.respondToInvitation(1L, 1L, InvitationStatus.DECLINED);
        
        assertThat(result).isNotNull();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.DECLINED);
        verify(attendanceService, never()).registerUser(any(User.class), any(Event.class));
        verify(notificationService).createNotification(organizer, notificationService.generateInvitationResponseMessage(invitee, event, InvitationStatus.DECLINED), NotificationType.INVITATION_DECLINED);
    }

    @Test
    void respondToInvitation_shouldThrowException_whenInviteeMismatch() {
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        invitee.setId(1L);
        User otherUser = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        otherUser.setId(2L);
        
        Invitation invitation = new Invitation();
        invitation.setInvitee(invitee);
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        
        assertThrows(ForbiddenOperationException.class, () -> invitationService.respondToInvitation(2L, 1L, InvitationStatus.ACCEPTED));
    }

    @Test
    void respondToInvitation_shouldThrowException_whenInvitationNotPending() {
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        invitee.setId(1L);
        
        Invitation invitation = new Invitation();
        invitation.setInvitee(invitee);
        invitation.setStatus(InvitationStatus.ACCEPTED);

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        
        assertThrows(InvalidStatusTransitionException.class, () -> invitationService.respondToInvitation(1L, 1L, InvitationStatus.ACCEPTED));
    }

    @Test
    void respondToInvitation_shouldThrowException_whenInvalidStatusResponse() {
        User invitee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        invitee.setId(1L);
        
        Invitation invitation = new Invitation();
        invitation.setInvitee(invitee);
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        
        assertThrows(InvalidStatusTransitionException.class, () -> invitationService.respondToInvitation(1L, 1L, InvitationStatus.EXPIRED));
    }

    @Test
    void listInvitationsReceivedByUserByStatus_shouldDefaultToPending_whenNullStatusProvided() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        
        invitationService.listInvitationsReceivedByUserByStatus(user, null);
        
        verify(invitationRepository).findByInviteeAndStatusOrderByInvitationSentDateAsc(user, InvitationStatus.PENDING);
    }

}
