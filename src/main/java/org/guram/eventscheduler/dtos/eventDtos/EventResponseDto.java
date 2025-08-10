package org.guram.eventscheduler.dtos.eventDtos;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.AttendanceStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record EventResponseDto (
        @NotNull Long id,
        @NotNull String title,
        String description,
        @NotNull LocalDateTime dateTime,
        @NotNull String location,
        boolean isCancelled,
        @NotNull List<UserSummaryDto> attendees,
        @NotNull List<UserSummaryDto> organizers,
        @NotNull Map<Long, AttendanceStatus> userAttendanceStatus
) {}
