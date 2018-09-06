package uk.gov.defra.datareturns.validation.smallcatches;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validate a {@link uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch} object
 *
 * @author Sam Gardner-Dell
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SmallCatchValidator.class)
@Documented
public @interface ValidSmallCatch {
    /**
     * Default constraint violation template
     *
     * @return the constraint violation template
     */
    String message() default "SMALL_CATCH_INVALID_UNSPECIFIED_REASON";

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
