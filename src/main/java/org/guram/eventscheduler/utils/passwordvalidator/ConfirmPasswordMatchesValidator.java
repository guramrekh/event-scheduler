package org.guram.eventscheduler.utils.passwordvalidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.guram.eventscheduler.dtos.userDtos.PasswordChangeDto;

public class ConfirmPasswordMatchesValidator implements ConstraintValidator<ConfirmPasswordMatches, PasswordChangeDto> {
    @Override
    public boolean isValid(PasswordChangeDto passwordChangeDto, ConstraintValidatorContext constraintValidatorContext) {
        if (passwordChangeDto.newPassword() == null || passwordChangeDto.confirmNewPassword() == null)
            return true;

        return passwordChangeDto.newPassword().equals(passwordChangeDto.confirmNewPassword());
    }
}
