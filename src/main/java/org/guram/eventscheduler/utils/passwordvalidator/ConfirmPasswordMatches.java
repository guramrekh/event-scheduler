package org.guram.eventscheduler.utils.passwordvalidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ConfirmPasswordMatchesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfirmPasswordMatches {

    String message() default "New password and confirm new password do not match";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
