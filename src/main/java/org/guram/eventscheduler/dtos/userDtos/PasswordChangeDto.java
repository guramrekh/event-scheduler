package org.guram.eventscheduler.dtos.userDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.guram.eventscheduler.utils.passwordvalidator.ConfirmPasswordMatches;

@ConfirmPasswordMatches
public record PasswordChangeDto(
        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String newPassword,

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String confirmNewPassword
) {}
