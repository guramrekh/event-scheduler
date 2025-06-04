package org.guram.eventscheduler.dtos.eventDtos;

import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;

import java.time.LocalDateTime;
import java.util.Set;

public record EventResponseDto (
        Long id,
        String title,
        String description,
        LocalDateTime dateTime,
        String location,
        Set<UserSummaryDto> organizers,
        Set<Long> attendanceIds
) {}
