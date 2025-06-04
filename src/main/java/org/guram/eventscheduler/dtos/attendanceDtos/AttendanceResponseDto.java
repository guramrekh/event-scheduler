package org.guram.eventscheduler.dtos.attendanceDtos;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.dtos.eventDtos.EventSummaryDto;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.AttendanceStatus;

public record AttendanceResponseDto(
        @NotNull Long id,
        UserSummaryDto user,
        EventSummaryDto event,
        @NotNull AttendanceStatus status
) {}
