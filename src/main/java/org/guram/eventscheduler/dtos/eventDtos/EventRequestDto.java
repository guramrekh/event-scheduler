package org.guram.eventscheduler.dtos.eventDtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventRequestDto(
        @NotBlank(message = "Title is mandatory")
        @Size(min = 1, max = 100, message = "Event title must be between 1 and 100 characters")
        String title,

        @Size(max = 1000, message = "Event description cannot be longer than 1000 characters")
        String description,

        @NotNull(message = "Date and time is mandatory")
        @Future(message = "Event date must be in the future")
        LocalDateTime dateTime,

        @NotBlank(message = "Location is mandatory")
        @Size(min = 1, max = 100, message = "Event location must be between 1 and 100 characters")
        String location
) {}
