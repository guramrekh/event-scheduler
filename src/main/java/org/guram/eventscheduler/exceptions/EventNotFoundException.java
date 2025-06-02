package org.guram.eventscheduler.exceptions;

public class EventNotFoundException extends ResourceNotFoundException {

    public EventNotFoundException(Long id) {
        super("Event", id);
    }
}
