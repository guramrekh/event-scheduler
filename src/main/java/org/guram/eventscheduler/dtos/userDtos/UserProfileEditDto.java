package org.guram.eventscheduler.dtos.userDtos;

import jakarta.validation.constraints.Size;

public record UserProfileEditDto(
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        String firstName,

        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName
) {}
