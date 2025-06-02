package org.guram.eventscheduler.DTOs.userDTOs;

import java.util.Set;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<Long> organizedEventIds,
        Set<Long> attendanceIds
) {}
