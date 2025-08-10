package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.Event;
import org.guram.eventscheduler.models.Invitation;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByInviteeAndEvent(User invitee, Event event);
    List<Invitation> findByInviteeAndStatusOrderByInvitationSentDateAsc(User invitee, InvitationStatus status);
}
