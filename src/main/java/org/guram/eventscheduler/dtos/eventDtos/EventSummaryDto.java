package org.guram.eventscheduler.dtos.eventDtos;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.User;

import java.time.LocalDateTime;
import java.util.List;

public record EventSummaryDto(
        @NotNull Long id,
        @NotNull String title,
        String description,
        @NotNull LocalDateTime dateTime,
        @NotNull String location,
        boolean isCancelled
) {}
