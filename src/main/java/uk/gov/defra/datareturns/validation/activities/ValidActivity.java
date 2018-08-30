package uk.gov.defra.datareturns.validation.activities;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validate an {@link uk.gov.defra.datareturns.data.model.activities.Activity} object
 *
 * @author Sam Gardner-Dell
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ActivityValidator.class)
@Documented
public @interface ValidActivity {
    /**
     * Default constraint violation template
     *
     * @return the constraint violation template
     */
    String message() default "ACTIVITY_INVALID_UNSPECIFIED_REASON";

    /**
     * Validation groups
     *
     * @return the groups that this validator is associated with
     */
    Class<?>[] groups() default {};

    /**
     * Validation payload
     *
     * @return the Payload
     */
    Class<? extends Payload>[] payload() default {};
}
