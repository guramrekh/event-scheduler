package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Event event1;

    @BeforeEach
    void setUp() {
        user1 = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        user2 = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        userRepository.saveAll(List.of(user1, user2));

        event1 = new Event("future event one", LocalDateTime.now().plusHours(1), "tbilisi");
        Event event2 = new Event("future event two", LocalDateTime.now().plusHours(3), "batumi");
        eventRepository.saveAll(List.of(event1, event2));

        Attendance attendance1 = new Attendance(user1, event1, AttendanceRole.ATTENDEE);
        Attendance attendance2 = new Attendance(user1, event2, AttendanceRole.ORGANIZER);

        attendanceRepository.saveAll(List.of(attendance1, attendance2));
    }


    @Test
    void findByUserAndRoleAndDateTimeAndStatus_shouldReturnUpcomingEvents_whenRoleNullAndUpcomingTrue() {
        List<Event> foundEvents = eventRepository.findByUserAndRoleAndDateTimeAndStatus(user1, null, true, false);

        assertThat(foundEvents)
                .extracting(Event::getTitle)
                .containsExactly("future event one", "future event two");
    }

    @Test
    void findByUserAndRoleAndDateTimeAndStatus_shouldFilterByRole_whenRoleProvided() {
        List<Event> foundEvents = eventRepository.findByUserAndRoleAndDateTimeAndStatus(user1, AttendanceRole.ORGANIZER, true, false);

        assertThat(foundEvents)
                .extracting(Event::getTitle)
                .containsExactly("future event two");
    }

    @Test
    void findByUserAndRoleAndDateTimeAndStatus_shouldReturnEmptyList_whenUserHasNoAttendances() {
        List<Event> foundEvents = eventRepository.findByUserAndRoleAndDateTimeAndStatus(user2, null, true, false);

        assertThat(foundEvents).isEmpty();
    }

    @Test
    void findByUserAndRoleAndDateTimeAndStatus_shouldIncludeCancelledEvents_whenCancelledTrue() {
        event1.setCancelled(true);
        eventRepository.save(event1);

        List<Event> foundEvents = eventRepository.findByUserAndRoleAndDateTimeAndStatus(user1, null, true, true);

        assertThat(foundEvents)
                .extracting(Event::getTitle)
                .containsExactly("future event one", "future event two");
    }

    @Test
    void findByUserAndRoleAndDateTimeAndStatus_shouldExcludeCancelledEvents_whenCancelledFalse() {
        event1.setCancelled(true);
        eventRepository.save(event1);

        List<Event> foundEvents = eventRepository.findByUserAndRoleAndDateTimeAndStatus(user1, null, true, false);

        assertThat(foundEvents)
                .extracting(Event::getTitle)
                .containsExactly("future event two");
    }

}
