package org.guram.eventscheduler.DTOs.attendanceDTOs;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.DTOs.eventDTOs.EventSummaryDto;
import org.guram.eventscheduler.DTOs.userDTOs.UserSummaryDto;
import org.guram.eventscheduler.models.AttendanceStatus;

public record AttendanceResponseDto(
        @NotNull Long id,
        UserSummaryDto user,
        EventSummaryDto event,
        @NotNull AttendanceStatus status
) {}
