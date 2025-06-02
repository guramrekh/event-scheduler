package org.guram.eventscheduler.DTOs.eventDTOs;

import org.guram.eventscheduler.DTOs.userDTOs.OrganizerDto;

import java.time.LocalDateTime;
import java.util.Set;

public record EventResponseDto (
        Long id,
        String title,
        String description,
        LocalDateTime dateTime,
        String location,
        Set<OrganizerDto> organizers,
        Set<Long> attendanceIds
) {}
