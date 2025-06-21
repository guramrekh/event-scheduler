package org.guram.eventscheduler.dtos.eventDtos;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.models.AttendanceRole;

public record EventWithRoleDto(
        @NotNull EventResponseDto event,
        @NotNull AttendanceRole role
) {}
