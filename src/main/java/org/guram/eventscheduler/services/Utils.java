package org.guram.eventscheduler.services;

import org.guram.eventscheduler.models.Event;

public class Utils {
    public static void checkIsOrganizer(Long actorUserId, Event event) {
        boolean actorIsOrganizer = event.getOrganizers().stream()
                .anyMatch(u -> u.getId().equals(actorUserId));
        if (!actorIsOrganizer) {
            throw new IllegalStateException("User (ID=" + actorUserId + ") is not an organizer for this event.");
        }
    }
}
