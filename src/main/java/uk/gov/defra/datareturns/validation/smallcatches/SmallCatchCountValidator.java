package uk.gov.defra.datareturns.validation.smallcatches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;

/**
 * Validate a {@link SmallCatchCount} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class SmallCatchCountValidator extends AbstractConstraintValidator<ValidSmallCatchCount, SmallCatchCount> {
    @Override
    public void initialize(final ValidSmallCatchCount constraintAnnotation) {
        super.addChecks(this::checkCountMethod, this::checkCountGreaterThanZero);
    }

    /**
     * Check that a method has been provided
     *
     * @param count   the {@link SmallCatchCount} to be validated
     * @param context the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkCountMethod(final SmallCatchCount count, final ConstraintValidatorContext context) {
        return count.getMethod() != null || handleError(context, "METHOD_REQUIRED", b -> b.addPropertyNode("method"));
    }

    /**
     * Check that the count is greater than zero
     *
     * @param count   the {@link SmallCatchCount} to be validated
     * @param context the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkCountGreaterThanZero(final SmallCatchCount count, final ConstraintValidatorContext context) {
        return count.getCount() > 0 || handleError(context, "NOT_GREATER_THAN_ZERO", b -> b.addPropertyNode("count"));
    }

    @Override
    public String getErrorPrefix() {
        return "SMALL_CATCH_COUNTS";
    }
}
