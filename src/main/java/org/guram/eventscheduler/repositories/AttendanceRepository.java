package org.guram.eventscheduler.repositories;

import jakarta.transaction.Transactional;
import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserAndEvent(User user, Event event);
    List<Attendance> findByEventAndStatusOrderByEvent_DateTimeAsc(Event event, AttendanceStatus status);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Attendance a
        SET a.status = org.guram.eventscheduler.models.AttendanceStatus.ATTENDED
        WHERE a.event = :event
            AND a.status = org.guram.eventscheduler.models.AttendanceStatus.REGISTERED
    """)
    void markAllAsAttended(@Param("event") Event event);

}
