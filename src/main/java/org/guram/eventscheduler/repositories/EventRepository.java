package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
        SELECT a.event FROM Attendance a
        WHERE a.user = :user
            AND (
                a.status = org.guram.eventscheduler.models.AttendanceStatus.REGISTERED
             OR a.status = org.guram.eventscheduler.models.AttendanceStatus.ATTENDED
            )
            AND (
                  (:role IS NULL AND a.role IN ('ORGANIZER', 'ATTENDEE'))
               OR (:role IS NOT NULL AND a.role = :role)
            )
            AND (
                  :cancelled = TRUE
               OR (a.event.isCancelled = FALSE)
            )
            AND (
                  (:upcoming = TRUE  AND a.event.dateTime > CURRENT_TIMESTAMP)
               OR (:upcoming = FALSE AND a.event.dateTime <= CURRENT_TIMESTAMP)
            )
        ORDER BY a.event.dateTime ASC
    """)
    List<Event> findByUserAndRoleAndDateTimeAndStatus(
            @Param("user") User user,
            @Param("role") AttendanceRole role,
            @Param("upcoming") boolean upcoming,
            @Param("cancelled") boolean cancelled
    );
}
