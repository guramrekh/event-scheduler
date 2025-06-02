package org.guram.eventscheduler.DTOs.userDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        String firstName,
        String lastName,

        @Email(message = "Email must be valid")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {}
