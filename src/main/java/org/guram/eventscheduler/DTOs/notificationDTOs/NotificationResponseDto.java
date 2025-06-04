package org.guram.eventscheduler.DTOs.notificationDTOs;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.DTOs.userDTOs.UserSummaryDto;
import org.guram.eventscheduler.models.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        @NotNull Long id,
        UserSummaryDto recipient,
        @NotNull String message,
        @NotNull NotificationType type,
        LocalDateTime createdAt
) {}
