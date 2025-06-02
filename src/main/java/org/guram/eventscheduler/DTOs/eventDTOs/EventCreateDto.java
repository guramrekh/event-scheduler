package org.guram.eventscheduler.DTOs.eventDTOs;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventCreateDto(
        @NotBlank(message = "Title is mandatory")
        String title,

        String description,

        @NotNull(message = "Date and time is mandatory")
        @Future(message = "Event date must be in the future")
        LocalDateTime dateTime,

        @NotBlank(message = "Location is mandatory")
        String location,

        @NotNull(message = "Organizer user ID is mandatory")
        Long organizerUserId
) {}
