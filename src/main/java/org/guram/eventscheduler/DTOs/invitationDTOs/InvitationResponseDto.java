package org.guram.eventscheduler.DTOs.invitationDTOs;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.DTOs.eventDTOs.EventSummaryDto;
import org.guram.eventscheduler.DTOs.userDTOs.UserSummaryDto;
import org.guram.eventscheduler.models.InvitationStatus;

import java.time.LocalDateTime;

public record InvitationResponseDto(
        @NotNull Long id,
        UserSummaryDto invitee,
        UserSummaryDto invitor,
        EventSummaryDto event,
        LocalDateTime invitationSentDate,
        InvitationStatus status
) {}
