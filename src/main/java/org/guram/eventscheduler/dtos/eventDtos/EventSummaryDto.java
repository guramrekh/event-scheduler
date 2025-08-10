package org.guram.eventscheduler.dtos.eventDtos;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EventSummaryDto(
        @NotNull Long id,
        @NotNull String title,
        String description,
        @NotNull LocalDateTime dateTime,
        @NotNull String location,
        boolean isCancelled
) {}
