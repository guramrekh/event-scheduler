package org.guram.eventscheduler.repositories;

import jakarta.transaction.Transactional;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndEvent(User user, Event event);
    List<Attendance> findByEventAndStatusOrderByEvent_DateTimeAsc(Event event, AttendanceStatus status);

    @Query("""
        SELECT u
        FROM Attendance a
        JOIN a.user u
        WHERE a.event = :event
            AND a.role  = :role
    """)
    List<User> findUserByEventAndAttendanceRole(
            @Param("event") Event event,
            @Param("role") AttendanceRole role
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE Attendance a
        SET a.status = :attended
        WHERE a.event = :event
            AND a.status = :registered
    """)
    void markAllAsAttended(
            @Param("event") Event event,
            @Param("registered") AttendanceStatus registered,
            @Param("attended") AttendanceStatus attended
    );

}
