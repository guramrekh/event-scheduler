package org.guram.eventscheduler.dtos.eventDtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventEditDto(
        @Size(min = 3, max = 100, message = "Event title must be between 3 and 100 characters")
        String title,

        @Size(max = 1000, message = "Event description cannot be longer than 1000 characters")
        String description,

        @Future(message = "Event date must be in the future")
        LocalDateTime dateTime,

        @Size(min = 3, max = 100, message = "Event location must be between 3 and 100 characters")
        String location
) {}
