package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.Invitation;
import org.guram.eventscheduler.models.InvitationStatus;
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
public class InvitationRepositoryTest {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User user1;
    private User user2;
    private User user3;
    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        user1 = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        user2 = new User("jane", "ear", "jane.ear@email.com", "<PASSWORD>");
        user3 = new User("carol", "brown", "carol.brown@email.com", "<PASSWORD>");
        userRepository.saveAll(List.of(user1, user2, user3));

        event1 = new Event("event alpha", LocalDateTime.now().plusDays(1), "tbilisi");
        event2 = new Event("event beta", LocalDateTime.now().plusDays(2), "batumi");
        Event event3 = new Event("event gamma", LocalDateTime.now().plusDays(3), "kutaisi");
        eventRepository.saveAll(List.of(event1, event2, event3));

        Invitation invitation1 = new Invitation();
        invitation1.setInvitee(user2);
        invitation1.setInvitor(user1);
        invitation1.setEvent(event1);

        Invitation invitation2 = new Invitation();
        invitation2.setInvitee(user2);
        invitation2.setInvitor(user1);
        invitation2.setEvent(event2);
        invitation2.setStatus(InvitationStatus.ACCEPTED);

        Invitation invitation3 = new Invitation();
        invitation3.setInvitee(user3);
        invitation3.setInvitor(user1);
        invitation3.setEvent(event1);
        invitation3.setStatus(InvitationStatus.DECLINED);

        invitationRepository.saveAll(List.of(invitation1, invitation2, invitation3));
    }


    @Test
    void findByInviteeAndEvent_shouldReturnInvitation_whenCombinationExistsInDb() {
        Optional<Invitation> optionalInvitation = invitationRepository.findByInviteeAndEvent(user2, event1);

        assertThat(optionalInvitation)
                .isPresent()
                .get()
                .extracting(Invitation::getInvitee)
                .isEqualTo(user2);
    }

    @Test
    void findByInviteeAndEvent_shouldReturnEmptyOptional_whenCombinationNotExistsInDb() {
        Optional<Invitation> optionalInvitation = invitationRepository.findByInviteeAndEvent(user3, event2);

        assertThat(optionalInvitation).isEmpty();
    }

    @Test
    void findByInviteeAndStatusOrderByInvitationSentDateAsc_shouldReturnCorrectInvitationsOrdered_whenInviteeAndStatusMatch() {
        List<Invitation> pendingInvitations = invitationRepository.findByInviteeAndStatusOrderByInvitationSentDateAsc(user2, InvitationStatus.PENDING);

        assertThat(pendingInvitations)
                .hasSize(1)
                .extracting(Invitation::getStatus)
                .containsOnly(InvitationStatus.PENDING);
    }

    @Test
    void findByInviteeAndStatusOrderByInvitationSentDateAsc_shouldReturnEmptyList_whenNoInvitationsMatchStatus() {
        List<Invitation> declinedInvitations = invitationRepository.findByInviteeAndStatusOrderByInvitationSentDateAsc(user2, InvitationStatus.DECLINED);

        assertThat(declinedInvitations).isEmpty();
    }

}
