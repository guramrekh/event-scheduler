package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.EventNotFoundException;
import org.guram.eventscheduler.exceptions.ResourceNotFoundException;
import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.AttendanceRepository;
import org.guram.eventscheduler.repositories.EventRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepo;

    @Mock
    private EventRepository eventRepo;

    @InjectMocks
    private AttendanceService attendanceService;


    @Test
    void registerUser_shouldSucceed_whenNoExistingAttendance() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();

        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(attendanceRepo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        attendanceService.registerUser(user, event);

        ArgumentCaptor<Attendance> attendanceArgumentCaptor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepo).save(attendanceArgumentCaptor.capture());

        Attendance savedAttendance = attendanceArgumentCaptor.getValue();
        assertThat(savedAttendance.getUser()).isEqualTo(user);
        assertThat(savedAttendance.getEvent()).isEqualTo(event);
        assertThat(savedAttendance.getRole()).isEqualTo(AttendanceRole.ATTENDEE);
        assertThat(savedAttendance.getStatus()).isEqualTo(AttendanceStatus.REGISTERED);
    }

    @Test
    void registerUser_shouldDoNothing_whenAlreadyRegistered() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();
        Attendance existingAttendance = new Attendance(user, event, AttendanceRole.ATTENDEE);
        existingAttendance.setStatus(AttendanceStatus.REGISTERED);

        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.of(existingAttendance));

        attendanceService.registerUser(user, event);

        verify(attendanceRepo, never()).save(any(Attendance.class));
    }

    @Test
    void registerUser_shouldReactivate_whenPreviouslyWithdrawn() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();
        Attendance existingAttendance = new Attendance(user, event, AttendanceRole.ATTENDEE);
        existingAttendance.setStatus(AttendanceStatus.WITHDRAWN);

        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.of(existingAttendance));
        when(attendanceRepo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        attendanceService.registerUser(user, event);

        verify(attendanceRepo).save(existingAttendance);
        assertThat(existingAttendance.getStatus()).isEqualTo(AttendanceStatus.REGISTERED);
    }

    @Test
    void registerUser_shouldThrowException_whenAlreadyAttended() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();
        Attendance existingAttendance = new Attendance(user, event, AttendanceRole.ATTENDEE);
        existingAttendance.setStatus(AttendanceStatus.ATTENDED);

        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.of(existingAttendance));

        assertThrows(ConflictException.class, () -> attendanceService.registerUser(user, event));

        verify(attendanceRepo, never()).save(any(Attendance.class));
    }

    @Test
    void withdrawFromEvent_shouldSucceed_whenValidInput() {
        User user = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        Event event = new Event();
        event.setId(1L);
        Attendance attendance = new Attendance(user, event, AttendanceRole.ATTENDEE);
        attendance.setStatus(AttendanceStatus.REGISTERED);

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.of(attendance));
        when(attendanceRepo.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponseDto result = attendanceService.withdrawFromEvent(user, 1L);

        assertThat(result).isNotNull();
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.WITHDRAWN);
        verify(attendanceRepo).save(attendance);
    }

    @Test
    void withdrawFromEvent_shouldThrowException_whenEventNotFound() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> attendanceService.withdrawFromEvent(user, 1L));

        verify(attendanceRepo, never()).findByUserAndEvent(any(User.class), any(Event.class));
    }

    @Test
    void withdrawFromEvent_shouldThrowException_whenAttendanceNotFound() {
        User user = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        Event event = new Event();

        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        assertThrows(ConflictException.class, () -> attendanceService.withdrawFromEvent(user, 1L));
    }

    @Test
    void getAttendanceRole_shouldReturnRole_whenAttendanceExists() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();
        Attendance attendance = new Attendance(user, event, AttendanceRole.ORGANIZER);

        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.of(attendance));

        AttendanceRole result = attendanceService.getAttendanceRole(user, event);

        assertThat(result).isEqualTo(AttendanceRole.ORGANIZER);
    }

    @Test
    void getAttendanceRole_shouldThrowException_whenAttendanceNotExists() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        Event event = new Event();

        when(attendanceRepo.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> attendanceService.getAttendanceRole(user, event));
    }

}
