package edu.zut.bookrider.validation.annotation;

import edu.zut.bookrider.validation.validator.NotEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmailValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmail {

    String message() default "Username cannot be an email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}