package org.guram.eventscheduler.DTOs.eventDTOs;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record EventEditDto(
        @NotBlank(message = "Title is mandatory")
        @Size(min = 3, max = 100, message = "Event title must be between 3 and 100 characters")
        String title,

        @Size(max = 1000, message = "Event description cannot be longer than 1000 characters")
        String description,

        @NotNull(message = "Date and time is mandatory")
        @Future(message = "Event date must be in the future")
        LocalDateTime dateTime,

        @NotBlank(message = "Location is mandatory")
        @Size(min = 3, max = 100, message = "Event location must be between 3 and 100 characters")
        String location
) {}
