package org.guram.eventscheduler.DTOs.eventDTOs;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;

public record EventEditDto(
          String title,
          String description,
          String location,

          @FutureOrPresent(message = "Event date must be in the future or present")
          LocalDateTime dateTime
) {}
