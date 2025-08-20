package org.guram.eventscheduler.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found (ID=" + id + ")");
    }
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
