package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
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
    public AttendanceResponseDto registerUser(User user, Event event) {
        Optional<Attendance> existingAttendance = attendanceRepo.findByUserAndEvent(user, event);
        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            if (attendance.getStatus() == AttendanceStatus.REGISTERED) {
                return Utils.mapAttendanceToResponseDto(attendance);
            }
            if (attendance.getStatus() == AttendanceStatus.CANCELLED) {
                attendance.setStatus(AttendanceStatus.REGISTERED);
                Attendance updated = attendanceRepo.save(attendance);
                return Utils.mapAttendanceToResponseDto(updated);
            }
            throw new ConflictException("User (ID=" + user.getId() + ") already has an attendance record for event (ID=" + event.getId() + ") with status " + attendance.getStatus() + ".");
        }

        Attendance newAttendance = new Attendance();
        newAttendance.setUser(user);
        newAttendance.setEvent(event);
        newAttendance.setStatus(AttendanceStatus.REGISTERED);

        user.getAttendances().add(newAttendance);
        event.getAttendances().add(newAttendance);

        Attendance savedAttendance = attendanceRepo.save(newAttendance);
        return Utils.mapAttendanceToResponseDto(savedAttendance);
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
