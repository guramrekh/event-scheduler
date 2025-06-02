package org.guram.eventscheduler.services;

import org.guram.eventscheduler.DTOs.attendanceDTOs.AttendanceResponseDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.EventNotFoundException;
import org.guram.eventscheduler.exceptions.InvalidStatusTransitionException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.AttendanceRepository;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepo,
                             UserRepository userRepo,
                             EventRepository eventRepo) {
        this.attendanceRepo = attendanceRepo;
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
    }


    @Transactional
    public AttendanceResponseDto inviteUser(Long eventId, Long actorUserId, Long inviteeUserId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Utils.checkIsOrganizer(actorUserId, event);

        User invitee = userRepo.findById(inviteeUserId)
                .orElseThrow(() -> new UserNotFoundException(inviteeUserId));

        Optional<Attendance> attendanceOpt = attendanceRepo.findByUserAndEvent(invitee, event);
        if (attendanceOpt.isPresent()) {
            throw new ConflictException("User (ID=" + inviteeUserId + ") has an attendance record with status " + attendanceOpt.get().getStatus() + " for this event.");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(invitee);
        attendance.setEvent(event);
        attendance.setStatus(AttendanceStatus.INVITED);

        Attendance savedAttendance = attendanceRepo.save(attendance);
        return Utils.mapAttendanceToResponseDto(savedAttendance);
    }

    @Transactional
    public AttendanceResponseDto respondToInvitation(Long eventId, Long inviteeUserId, AttendanceStatus newStatus) {
        if (newStatus != AttendanceStatus.REGISTERED && newStatus != AttendanceStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Invalid status. Must be REGISTERED or CANCELLED.");
        }

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        User invitee = userRepo.findById(inviteeUserId)
                .orElseThrow(() -> new UserNotFoundException(inviteeUserId));

        Attendance attendance = attendanceRepo.findByUserAndEvent(invitee, event)
                .orElseThrow(() -> new ConflictException("No pending invitation for this user/event."));

        if (attendance.getStatus() != AttendanceStatus.INVITED) {
            throw new InvalidStatusTransitionException(
                    "Invitation already responded to (status=" + attendance.getStatus() + ").");
        }

        attendance.setStatus(newStatus);
        Attendance updatedAttendance = attendanceRepo.save(attendance);
        return Utils.mapAttendanceToResponseDto(updatedAttendance);
    }

    @Transactional
    public AttendanceResponseDto cancelAttendance(Long eventId, Long userId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Attendance attendance = attendanceRepo.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ConflictException("No attendance record to cancel."));

        attendance.setStatus(AttendanceStatus.CANCELLED);
        Attendance cancelledAttendance = attendanceRepo.save(attendance);
        return Utils.mapAttendanceToResponseDto(cancelledAttendance);
    }

    @Transactional
    public AttendanceResponseDto markAttended(Long eventId, Long actorUserId, Long attendeeUserId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Utils.checkIsOrganizer(actorUserId, event);

        User attendee = userRepo.findById(attendeeUserId)
                .orElseThrow(() -> new UserNotFoundException(attendeeUserId));

        Attendance attendance = attendanceRepo.findByUserAndEvent(attendee, event)
                .orElseThrow(() -> new ConflictException("No attendance record for this user/event."));

        if (attendance.getStatus() != AttendanceStatus.REGISTERED) {
            throw new InvalidStatusTransitionException(
                    "Cannot mark attendance because current status is " + attendance.getStatus());
        }

        attendance.setStatus(AttendanceStatus.ATTENDED);
        Attendance updatedAttendance = attendanceRepo.save(attendance);
        return Utils.mapAttendanceToResponseDto(updatedAttendance);
    }

    public List<AttendanceResponseDto> listAttendancesByStatus(Long eventId, AttendanceStatus status) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        return attendanceRepo.findByEventAndStatus(event, status).stream()
                .map(Utils::mapAttendanceToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponseDto> listUserRegistrations(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return attendanceRepo.findByUserAndStatus(user, AttendanceStatus.REGISTERED).stream()
                .map(Utils::mapAttendanceToResponseDto)
                .collect(Collectors.toList());
    }
}
