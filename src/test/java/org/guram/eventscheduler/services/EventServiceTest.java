package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.eventDtos.EventRequestDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventWithRoleDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.EventNotFoundException;
import org.guram.eventscheduler.exceptions.ForbiddenOperationException;
import org.guram.eventscheduler.exceptions.InvalidStatusTransitionException;
import org.guram.eventscheduler.exceptions.ResourceNotFoundException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.Invitation;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.models.NotificationType;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.AttendanceRepository;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AttendanceRepository attendanceRepo;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private EventService eventService;


    @Test
    void createEvent_shouldSucceed_whenValidInput() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        var eventRequestDto = new EventRequestDto("test event", "test description", LocalDateTime.now().plusDays(1), "tbilisi");

        when(eventRepo.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponseDto result = eventService.createEvent(organizer, eventRequestDto);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepo).save(eventArgumentCaptor.capture());

        Event savedEvent = eventArgumentCaptor.getValue();
        assertThat(savedEvent.getTitle()).isEqualTo("test event");
        assertThat(savedEvent.getDescription()).isEqualTo("test description");
        assertThat(savedEvent.getLocation()).isEqualTo("tbilisi");
        assertThat(result).isNotNull();
    }

    @Test
    void makeAttendeeOrganizer_shouldSucceed_whenValidInput() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        User newOrganizer = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        newOrganizer.setId(2L);
        Event event = new Event();

        Attendance actorAttendance = new Attendance(actor, event, AttendanceRole.ORGANIZER);
        Attendance targetAttendance = new Attendance(newOrganizer, event, AttendanceRole.ATTENDEE);
        event.getAttendances().add(actorAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(newOrganizer));
        when(attendanceRepo.findByUserAndEvent(newOrganizer, event)).thenReturn(Optional.of(targetAttendance));

        EventResponseDto result = eventService.makeAttendeeOrganizer(actor, 2L, 1L);

        assertThat(result).isNotNull();
        assertThat(targetAttendance.getRole()).isEqualTo(AttendanceRole.ORGANIZER);
        verify(notificationService).createNotification(newOrganizer, notificationService.generateAddedAsOrganizerMessage(actor, event), NotificationType.ADDED_AS_ORGANIZER);
    }

    @Test
    void makeAttendeeOrganizer_shouldReturnEvent_whenUserAlreadyOrganizer() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        User targetUser = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        targetUser.setId(2L);
        Event event = new Event();

        Attendance actorAttendance = new Attendance(actor, event, AttendanceRole.ORGANIZER);
        Attendance targetAttendance = new Attendance(targetUser, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(actorAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(targetUser));
        when(attendanceRepo.findByUserAndEvent(targetUser, event)).thenReturn(Optional.of(targetAttendance));

        EventResponseDto result = eventService.makeAttendeeOrganizer(actor, 2L, 1L);

        assertThat(result).isNotNull();
        verify(notificationService, never()).createNotification(any(User.class), anyString(), any(NotificationType.class));
    }

    @Test
    void makeAttendeeOrganizer_shouldThrowException_whenEventNotFound() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.makeAttendeeOrganizer(actor, 2L, 1L));
    }

    @Test
    void makeAttendeeOrganizer_shouldThrowException_whenActorNotOrganizer() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        User targetUser = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        targetUser.setId(2L);
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.makeAttendeeOrganizer(actor, 2L, 1L));
    }

    @Test
    void makeAttendeeOrganizer_shouldThrowException_whenUserNotFound() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        Event event = new Event();

        Attendance actorAttendance = new Attendance(actor, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(actorAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> eventService.makeAttendeeOrganizer(actor, 2L, 1L));
    }

    @Test
    void makeAttendeeOrganizer_shouldThrowException_whenAttendanceNotFound() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        User targetUser = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        targetUser.setId(2L);
        Event event = new Event();

        Attendance actorAttendance = new Attendance(actor, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(actorAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(targetUser));
        when(attendanceRepo.findByUserAndEvent(targetUser, event)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.makeAttendeeOrganizer(actor, 2L, 1L));
    }

    @Test
    void removeOrganizerRole_shouldSucceed_whenValidInput() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        User targetUser = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        targetUser.setId(2L);
        Event event = new Event();

        Attendance actorAttendance = new Attendance(actor, event, AttendanceRole.ORGANIZER);
        Attendance targetAttendance = new Attendance(targetUser, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(actorAttendance);
        event.getAttendances().add(targetAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(targetUser));
        when(attendanceRepo.findByUserAndEvent(targetUser, event)).thenReturn(Optional.of(targetAttendance));

        EventResponseDto result = eventService.removeOrganizerRole(actor, 2L, 1L);

        assertThat(result).isNotNull();
        assertThat(targetAttendance.getRole()).isEqualTo(AttendanceRole.ATTENDEE);
        verify(notificationService).createNotification(targetUser, notificationService.generateRemovedAsOrganizerMessage(actor, event), NotificationType.REMOVED_AS_ORGANIZER);
    }

    @Test
    void removeOrganizerRole_shouldThrowException_whenUserAlreadyAttendee() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        User targetUser = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        targetUser.setId(2L);
        Event event = new Event();

        Attendance actorAttendance = new Attendance(actor, event, AttendanceRole.ORGANIZER);
        Attendance targetAttendance = new Attendance(targetUser, event, AttendanceRole.ATTENDEE);
        event.getAttendances().add(actorAttendance);
        event.getAttendances().add(targetAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(targetUser));

        assertThrows(ForbiddenOperationException.class, () -> eventService.removeOrganizerRole(actor, 2L, 1L));
    }

    @Test
    void removeOrganizerRole_shouldThrowException_whenEventNotFound() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.removeOrganizerRole(actor, 2L, 1L));
    }

    @Test
    void removeOrganizerRole_shouldThrowException_whenActorNotOrganizer() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.removeOrganizerRole(actor, 2L, 1L));
    }

    @Test
    void removeOrganizerRole_shouldThrowException_whenUserNotFound() {
        User actor = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        actor.setId(1L);
        Event event = new Event();

        Attendance actorAttendance = new Attendance(actor, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(actorAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> eventService.removeOrganizerRole(actor, 2L, 1L));
    }

    @Test
    void kickUserFromEvent_shouldSucceed_whenValidInput() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        User userToKick = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        userToKick.setId(2L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        Attendance kickAttendance = new Attendance(userToKick, event, AttendanceRole.ATTENDEE);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(userToKick));
        when(attendanceRepo.findByUserAndEvent(userToKick, event)).thenReturn(Optional.of(kickAttendance));

        EventResponseDto result = eventService.kickUserFromEvent(organizer, 2L, 1L);

        assertThat(result).isNotNull();
        assertThat(kickAttendance.getStatus()).isEqualTo(AttendanceStatus.KICKED);
        verify(notificationService).createNotification(userToKick, notificationService.generateKickedOutFromEventMessage(organizer, event), NotificationType.REMOVED_AS_ORGANIZER);
    }

    @Test
    void kickUserFromEvent_shouldThrowException_whenEventNotFound() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.kickUserFromEvent(organizer, 2L, 1L));
    }

    @Test
    void kickUserFromEvent_shouldThrowException_whenOrganizerNotOrganizer() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.kickUserFromEvent(organizer, 2L, 1L));
    }

    @Test
    void kickUserFromEvent_shouldThrowException_whenUserNotFound() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> eventService.kickUserFromEvent(organizer, 2L, 1L));
    }

    @Test
    void kickUserFromEvent_shouldThrowException_whenAttendanceNotFound() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        User userToKick = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        userToKick.setId(2L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(userToKick));
        when(attendanceRepo.findByUserAndEvent(userToKick, event)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.kickUserFromEvent(organizer, 2L, 1L));
    }

    @Test
    void editEvent_shouldSucceed_whenValidInputWithoutNotifications() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepo.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var eventRequestDto = new EventRequestDto("updated title", "updated description", LocalDateTime.now().plusDays(2), "batumi");
        EventResponseDto result = eventService.editEvent(1L, 1L, eventRequestDto, false);

        assertThat(result).isNotNull();
        assertThat(event.getTitle()).isEqualTo("updated title");
        assertThat(event.getDescription()).isEqualTo("updated description");
        assertThat(event.getLocation()).isEqualTo("batumi");
        verify(eventRepo).save(event);
        verify(notificationService, never()).createNotification(any(User.class), anyString(), any(NotificationType.class));
    }

    @Test
    void editEvent_shouldSucceed_whenValidInputWithNotifications() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        User attendee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        User invitee = new User("bob", "smith", "bob.smith@email.com", "<PASSWORD>");
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        Attendance attendeeAttendance = new Attendance(attendee, event, AttendanceRole.ATTENDEE);
        attendeeAttendance.setStatus(AttendanceStatus.REGISTERED);
        event.getAttendances().add(organizerAttendance);

        Invitation invitation = new Invitation();
        invitation.setInvitee(invitee);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setEvent(event);
        event.getInvitations().add(invitation);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(attendanceRepo.findByEventAndStatusOrderByEvent_DateTimeAsc(event, AttendanceStatus.REGISTERED)).thenReturn(List.of(attendeeAttendance));
        when(eventRepo.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.generateEventUpdatedMessage(any(Event.class))).thenReturn("Test event updated message");

        var eventRequestDto = new EventRequestDto("updated title", "updated description", LocalDateTime.now().plusDays(2), "batumi");
        EventResponseDto result = eventService.editEvent(1L, 1L, eventRequestDto, true);

        assertThat(result).isNotNull();
        verify(eventRepo).save(event);
        verify(notificationService, times(2)).createNotification(any(User.class), anyString(), any(NotificationType.class));
    }

    @Test
    void editEvent_shouldThrowException_whenEventNotFound() {
        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        var eventRequestDto = new EventRequestDto("title", "description", LocalDateTime.now().plusDays(1), "location");
        assertThrows(EventNotFoundException.class, () -> eventService.editEvent(1L, 1L, eventRequestDto, false));
    }

    @Test
    void editEvent_shouldThrowException_whenUserNotOrganizer() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        var eventRequestDto = new EventRequestDto("title", "description", LocalDateTime.now().plusDays(1), "location");
        assertThrows(ForbiddenOperationException.class, () -> eventService.editEvent(1L, 1L, eventRequestDto, false));
    }

    @Test
    void cancelEvent_shouldSucceed_whenValidInput() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        User attendee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        User invitee = new User("bob", "smith", "bob.smith@email.com", "<PASSWORD>");
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        Attendance attendeeAttendance = new Attendance(attendee, event, AttendanceRole.ATTENDEE);
        attendeeAttendance.setStatus(AttendanceStatus.REGISTERED);
        event.getAttendances().add(organizerAttendance);

        Invitation invitation = new Invitation();
        invitation.setInvitee(invitee);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setEvent(event);
        event.getInvitations().add(invitation);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(attendanceRepo.findByEventAndStatusOrderByEvent_DateTimeAsc(event, AttendanceStatus.REGISTERED)).thenReturn(List.of(attendeeAttendance));
        when(eventRepo.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.generateEventCancelledMessage(any(Event.class))).thenReturn("Test event cancelled message");

        eventService.cancelEvent(1L, 1L);

        assertThat(event.isCancelled()).isTrue();
        verify(eventRepo).save(event);
        verify(notificationService, times(2)).createNotification(any(User.class), anyString(), any(NotificationType.class));
    }

    @Test
    void cancelEvent_shouldThrowException_whenEventNotFound() {
        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.cancelEvent(1L, 1L));
    }

    @Test
    void cancelEvent_shouldThrowException_whenUserNotOrganizer() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.cancelEvent(1L, 1L));
    }

    @Test
    void getFilteredEventsWithRole_shouldReturnEvents_whenUpcomingTimeframe() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();

        when(eventRepo.findByUserAndRoleAndDateTimeAndStatus(user, null, true, false)).thenReturn(List.of(event));
        when(attendanceService.getAttendanceRole(user, event)).thenReturn(AttendanceRole.ATTENDEE);

        List<EventWithRoleDto> result = eventService.getFilteredEventsWithRole(user, null, "UPCOMING", false);

        assertThat(result).hasSize(1);
        verify(eventRepo).findByUserAndRoleAndDateTimeAndStatus(user, null, true, false);
    }

    @Test
    void getFilteredEventsWithRole_shouldReturnEvents_whenPastTimeframe() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();

        when(eventRepo.findByUserAndRoleAndDateTimeAndStatus(user, AttendanceRole.ORGANIZER, false, true)).thenReturn(List.of(event));
        when(attendanceService.getAttendanceRole(user, event)).thenReturn(AttendanceRole.ORGANIZER);

        List<EventWithRoleDto> result = eventService.getFilteredEventsWithRole(user, AttendanceRole.ORGANIZER, "PAST", true);

        assertThat(result).hasSize(1);
        verify(eventRepo).findByUserAndRoleAndDateTimeAndStatus(user, AttendanceRole.ORGANIZER, false, true);
    }

    @Test
    void getFilteredEventsWithRole_shouldThrowException_whenInvalidTimeframe() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        assertThrows(IllegalArgumentException.class, () -> eventService.getFilteredEventsWithRole(user, null, "INVALID", false));
    }

    @Test
    void markAttended_shouldSucceed_whenValidInput() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        User attendee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        attendee.setId(2L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        Attendance attendeeAttendance = new Attendance(attendee, event, AttendanceRole.ATTENDEE);
        attendeeAttendance.setStatus(AttendanceStatus.REGISTERED);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(attendee));
        when(attendanceRepo.findByUserAndEvent(attendee, event)).thenReturn(Optional.of(attendeeAttendance));

        EventResponseDto result = eventService.markAttended(organizer, 2L, 1L);

        assertThat(result).isNotNull();
        assertThat(attendeeAttendance.getStatus()).isEqualTo(AttendanceStatus.ATTENDED);
    }

    @Test
    void markAttended_shouldThrowException_whenEventNotFound() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.markAttended(organizer, 2L, 1L));
    }

    @Test
    void markAttended_shouldThrowException_whenOrganizerNotOrganizer() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.markAttended(organizer, 2L, 1L));
    }

    @Test
    void markAttended_shouldThrowException_whenUserNotFound() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> eventService.markAttended(organizer, 2L, 1L));
    }

    @Test
    void markAttended_shouldThrowException_whenAttendanceNotFound() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        User attendee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        attendee.setId(2L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(attendee));
        when(attendanceRepo.findByUserAndEvent(attendee, event)).thenReturn(Optional.empty());

        assertThrows(ConflictException.class, () -> eventService.markAttended(organizer, 2L, 1L));
    }

    @Test
    void markAttended_shouldThrowException_whenInvalidStatus() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        User attendee = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        attendee.setId(2L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        Attendance attendeeAttendance = new Attendance(attendee, event, AttendanceRole.ATTENDEE);
        attendeeAttendance.setStatus(AttendanceStatus.WITHDRAWN);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(userRepo.findById(2L)).thenReturn(Optional.of(attendee));
        when(attendanceRepo.findByUserAndEvent(attendee, event)).thenReturn(Optional.of(attendeeAttendance));

        assertThrows(InvalidStatusTransitionException.class, () -> eventService.markAttended(organizer, 2L, 1L));
    }

    @Test
    void markAllAttended_shouldSucceed_whenValidInput() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        Attendance organizerAttendance = new Attendance(organizer, event, AttendanceRole.ORGANIZER);
        event.getAttendances().add(organizerAttendance);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        EventResponseDto result = eventService.markAllAttended(organizer, 1L);

        assertThat(result).isNotNull();
        verify(attendanceRepo).markAllAsAttended(event);
    }

    @Test
    void markAllAttended_shouldThrowException_whenEventNotFound() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.markAllAttended(organizer, 1L));
    }

    @Test
    void markAllAttended_shouldThrowException_whenUserNotOrganizer() {
        User organizer = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        organizer.setId(1L);
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.markAllAttended(organizer, 1L));
    }

}
