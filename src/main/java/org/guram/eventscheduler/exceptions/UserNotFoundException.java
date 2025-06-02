package org.guram.eventscheduler.exceptions;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long id) {
        super("User", id);
    }
    public UserNotFoundException(String message) {
        super(message);
    }
}
