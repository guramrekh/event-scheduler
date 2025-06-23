package org.guram.eventscheduler.dtos.userDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.guram.eventscheduler.utils.passwordvalidator.ConfirmPasswordMatches;

@ConfirmPasswordMatches
public record PasswordChangeDto(
        @NotNull
        String currentPassword,

        @NotNull
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String newPassword,

        @NotNull
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String confirmNewPassword
) {}
