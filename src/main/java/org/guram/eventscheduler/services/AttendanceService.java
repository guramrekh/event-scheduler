package org.guram.eventscheduler.services;

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
    public Attendance inviteUser(Long eventId, Long actorUserId, Long inviteeUserId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        Utils.checkIsOrganizer(actorUserId, event);

        User invitee = userRepo.findById(inviteeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invitee not found (ID=" + inviteeUserId + ")"));

        Optional<Attendance> existing = attendanceRepo.findByUserAndEvent(invitee, event);
        if (existing.isPresent()) {
            throw new IllegalStateException("User (ID=" + inviteeUserId + ") has already been invited or responded.");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(invitee);
        attendance.setEvent(event);
        attendance.setStatus(AttendanceStatus.INVITED);

        return attendanceRepo.save(attendance);
    }

    @Transactional
    public Attendance respondToInvitation(Long eventId, Long inviteeUserId, AttendanceStatus newStatus) {
        if (newStatus != AttendanceStatus.REGISTERED && newStatus != AttendanceStatus.CANCELLED) {
            throw new IllegalArgumentException("Invalid status. Must be REGISTERED or CANCELLED.");
        }

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        User invitee = userRepo.findById(inviteeUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found (ID=" + inviteeUserId + ")"));

        Attendance attendance = attendanceRepo.findByUserAndEvent(invitee, event)
                .orElseThrow(() -> new IllegalStateException("No pending invitation for this user/event."));

        if (attendance.getStatus() != AttendanceStatus.INVITED) {
            throw new IllegalStateException(
                    "Invitation already responded to (status=" + attendance.getStatus() + ").");
        }

        attendance.setStatus(newStatus);
        return attendanceRepo.save(attendance);
    }

    @Transactional
    public Attendance cancelAttendance(Long eventId, Long userId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found (ID=" + userId + ")"));

        Attendance attendance = attendanceRepo.findByUserAndEvent(user, event)
                .orElseThrow(() -> new IllegalStateException("No attendance record to cancel."));

        attendance.setStatus(AttendanceStatus.CANCELLED);
        return attendanceRepo.save(attendance);
    }

    @Transactional
    public Attendance markAttended(Long eventId, Long actorUserId, Long attendeeUserId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));

        Utils.checkIsOrganizer(actorUserId, event);

        User attendee = userRepo.findById(attendeeUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found (ID=" + attendeeUserId + ")"));

        Attendance attendance = attendanceRepo.findByUserAndEvent(attendee, event)
                .orElseThrow(() -> new IllegalStateException("No attendance record for this user/event."));

        if (attendance.getStatus() != AttendanceStatus.REGISTERED) {
            throw new IllegalStateException(
                    "Cannot mark attendance because current status is " + attendance.getStatus());
        }

        attendance.setStatus(AttendanceStatus.ATTENDED);
        return attendanceRepo.save(attendance);
    }

    public List<Attendance> listAttendancesByStatus(Long eventId, AttendanceStatus status) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found (ID=" + eventId + ")"));
        return attendanceRepo.findByEventAndStatus(event, status);
    }

    public List<Attendance> listUserRegistrations(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found (ID=" + userId + ")"));
        return attendanceRepo.findByUserAndStatus(user, AttendanceStatus.REGISTERED);
    }
}
