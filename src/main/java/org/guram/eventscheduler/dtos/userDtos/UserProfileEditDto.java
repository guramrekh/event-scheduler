package org.guram.eventscheduler.dtos.userDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserProfileEditDto(
        @NotNull(message = "First name is required")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        String firstName,

        @NotNull(message = "Last name is required")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @Size(max = 500, message = "Bio cannot be longer than 500 characters")
        String bio
) {}
