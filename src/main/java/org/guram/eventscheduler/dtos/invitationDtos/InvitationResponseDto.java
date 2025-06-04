package org.guram.eventscheduler.dtos.invitationDtos;

import jakarta.validation.constraints.NotNull;
import org.guram.eventscheduler.dtos.eventDtos.EventSummaryDto;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
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
