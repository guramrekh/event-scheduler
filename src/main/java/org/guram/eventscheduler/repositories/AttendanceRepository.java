package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndEvent(User user, Event event);
    List<Attendance> findByEventAndStatus(Event event, AttendanceStatus status);
    List<Attendance> findByUserAndStatus(User user, AttendanceStatus status);
}
