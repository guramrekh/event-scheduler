package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;

public class Utils {

    public static User getCurrentUser(UserDetails userDetails, UserService userService) {
        String email = userDetails.getUsername();
        return userService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email + ". This should not happen for an authenticated user."));
    }
}
