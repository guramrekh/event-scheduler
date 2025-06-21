package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.EventNotFoundException;
import org.guram.eventscheduler.exceptions.ResourceNotFoundException;
import org.guram.eventscheduler.models.*;
import org.guram.eventscheduler.repositories.AttendanceRepository;
import org.guram.eventscheduler.repositories.EventRepository;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final EventRepository eventRepo;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepo, EventRepository eventRepo) {
        this.attendanceRepo = attendanceRepo;
        this.eventRepo = eventRepo;
    }


    @Transactional
    public void registerUser(User user, Event event) {
        Optional<Attendance> existingAttendance = attendanceRepo.findByUserAndEvent(user, event);
        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            if (attendance.getStatus() == AttendanceStatus.REGISTERED) {
                Utils.mapAttendanceToResponseDto(attendance);
                return;
            }
            if (attendance.getStatus() == AttendanceStatus.WITHDRAWN ||
                    attendance.getStatus() == AttendanceStatus.KICKED ) {
                attendance.setStatus(AttendanceStatus.REGISTERED);
                Attendance updated = attendanceRepo.save(attendance);
                Utils.mapAttendanceToResponseDto(updated);
                return;
            }
            throw new ConflictException("User (ID=" + user.getId() + ") already has an attendance record for event (ID=" + event.getId() + ") with status " + attendance.getStatus() + ".");
        }

        Attendance newAttendance = new Attendance();
        newAttendance.setUser(user);
        newAttendance.setEvent(event);
        newAttendance.setRole(AttendanceRole.ATTENDEE);
        newAttendance.setStatus(AttendanceStatus.REGISTERED);

        user.getAttendances().add(newAttendance);
        event.getAttendances().add(newAttendance);

        Attendance savedAttendance = attendanceRepo.save(newAttendance);
        Utils.mapAttendanceToResponseDto(savedAttendance);
    }

    @Transactional
    public AttendanceResponseDto withdrawFromEvent(User currentUser, Long eventId) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Attendance attendance = attendanceRepo.findByUserAndEvent(currentUser, event)
                .orElseThrow(() -> new ConflictException("No attendance record to withdraw from."));

        attendance.setStatus(AttendanceStatus.WITHDRAWN);
        Attendance cancelledAttendance = attendanceRepo.save(attendance);
        return Utils.mapAttendanceToResponseDto(cancelledAttendance);
    }

    public AttendanceRole getAttendanceRole(User user, Event event) {
        Attendance attendance = attendanceRepo.findByUserAndEvent(user, event)
                .orElseThrow(() -> new ResourceNotFoundException(user.getId() + "is not attending the event: " + event.getId() + "."));

        return attendance.getRole();
    }
}
