package edu.zut.bookrider.validation.validator;

import edu.zut.bookrider.validation.annotation.NotEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmailValidator implements ConstraintValidator<NotEmail, String> {

    private static final String EMAIL_REGEX =
            "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.matches(EMAIL_REGEX);
    }
}