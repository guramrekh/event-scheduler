package org.guram.eventscheduler.DTOs.eventDTOs;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventSummaryDto(
        @NotNull Long id,
        String title,
        LocalDateTime dateTime,
        String location
) {}
