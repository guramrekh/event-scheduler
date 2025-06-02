package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime now);
}
