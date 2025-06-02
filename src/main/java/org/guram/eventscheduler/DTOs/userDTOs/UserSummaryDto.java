package org.guram.eventscheduler.DTOs.userDTOs;

import jakarta.validation.constraints.NotNull;

public record UserSummaryDto (
        @NotNull Long id,
        String firstName,
        String lastName,
        String email
) {}
