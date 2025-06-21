package org.guram.eventscheduler.dtos.userDtos;

import jakarta.validation.constraints.NotNull;

public record UserSummaryDto (
        @NotNull Long id,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String email
) {}
