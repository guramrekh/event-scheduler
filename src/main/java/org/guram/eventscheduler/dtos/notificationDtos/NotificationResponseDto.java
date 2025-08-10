package org.guram.eventscheduler.dtos.notificationDtos;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponseDto(
        @NotNull Long id,
        @NotNull UserSummaryDto recipient,
        @NotNull String message,
        @NotNull NotificationType type,
        @NotNull LocalDateTime createdAt,
        boolean read
) {}
