package org.guram.eventscheduler.dtos.userDtos;

import jakarta.validation.constraints.NotNull;

public record UserResponseDto(
        @NotNull Long id,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String email,
        String profilePictureUrl,
        int attendedEventsCount,
        int organizedEventsCount,
        int withdrawnFromEventsCount,
        int kickedOutFromEventsCount
) {}
