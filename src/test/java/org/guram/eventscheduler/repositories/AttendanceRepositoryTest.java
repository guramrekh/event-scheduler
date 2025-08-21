package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.Attendance;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User user1;
    private User user2;
    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        user1 = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        user2 = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        userRepository.saveAll(List.of(user1, user2));

        event1 = new Event("event one", LocalDateTime.now().plusHours(2), "tbilisi");
        event2 = new Event("event two", LocalDateTime.now().plusHours(4), "batumi");
        eventRepository.saveAll(List.of(event1, event2));

        Attendance attendance1 = new Attendance(user1, event1, AttendanceRole.ATTENDEE);
        Attendance attendance2 = new Attendance(user2, event1, AttendanceRole.ATTENDEE);
        Attendance attendance3 = new Attendance(user1, event2, AttendanceRole.ORGANIZER);
        attendance3.setStatus(AttendanceStatus.WITHDRAWN);

        attendanceRepository.saveAll(List.of(attendance1, attendance2, attendance3));
    }


    @Test
    void findByUserAndEvent_shouldReturnAttendance_whenCombinationExistsInDb() {
        Optional<Attendance> optionalAttendance = attendanceRepository.findByUserAndEvent(user1, event1);

        assertThat(optionalAttendance)
                .isPresent()
                .get()
                .extracting(Attendance::getUser)
                .isEqualTo(user1);

        assertThat(optionalAttendance)
                .isPresent()
                .get()
                .extracting(Attendance::getEvent)
                .isEqualTo(event1);
    }

    @Test
    void findByUserAndEvent_shouldReturnEmptyOptional_whenCombinationNotExistsInDb() {
        Optional<Attendance> optionalAttendance = attendanceRepository.findByUserAndEvent(user2, event2);

        assertThat(optionalAttendance).isEmpty();
    }

    @Test
    void findByEventAndStatusOrderByEvent_DateTimeAsc_shouldReturnCorrectAttendances_whenEventAndStatusMatch() {
        List<Attendance> foundAttendances = attendanceRepository.findByEventAndStatusOrderByEvent_DateTimeAsc(event1, AttendanceStatus.REGISTERED);

        assertThat(foundAttendances)
                .hasSize(2)
                .allMatch(a -> a.getEvent().equals(event1))
                .allMatch(a -> a.getStatus() == AttendanceStatus.REGISTERED);
    }

    @Test
    void findByEventAndStatusOrderByEvent_DateTimeAsc_shouldReturnEmptyList_whenNoAttendancesMatch() {
        List<Attendance> foundAttendances = attendanceRepository.findByEventAndStatusOrderByEvent_DateTimeAsc(event2, AttendanceStatus.KICKED);

        assertThat(foundAttendances).isEmpty();
    }

    @Test
    void markAllAsAttended_shouldUpdateOnlyRegisteredAttendancesForSpecificEvent_whenCalled() {
        attendanceRepository.markAllAsAttended(event1);

        assertThat(event1.getAttendances())
                .allMatch(a -> a.getStatus() == AttendanceStatus.ATTENDED);
    }

}
