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
    private static final String PROPERTY_METHOD = "method";
    private static final String PROPERTY_COUNT = "count";

    @Override
    public void initialize(final ValidSmallCatchCount constraintAnnotation) {
        super.addChecks(this::checkMethod, this::checkMethodPermissions, this::checkCount);
    }

    /**
     * Check that a method has been provided
     *
     * @param count   the {@link SmallCatchCount} to be validated
     * @param context the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMethod(final SmallCatchCount count, final ConstraintValidatorContext context) {
        return count.getMethod() != null || handleError(context, "METHOD_REQUIRED", PROPERTY_METHOD);
    }

    /**
     * Check that the user has sufficient authority to use the given method
     *
     * @param count   the {@link SmallCatchCount} to be validated
     * @param context the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMethodPermissions(final SmallCatchCount count, final ConstraintValidatorContext context) {
        return checkRestrictedEntity(count.getMethod(), "method", context);
    }

    /**
     * Check that the count provided is valid
     *
     * @param count   the {@link SmallCatchCount} to be validated
     * @param context the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkCount(final SmallCatchCount count, final ConstraintValidatorContext context) {
        if (count.getCount() == null) {
            return handleError(context, "COUNT_REQUIRED", PROPERTY_COUNT);
        }
        return count.getCount() > 0 || handleError(context, "NOT_GREATER_THAN_ZERO", PROPERTY_COUNT);
    }

    @Override
    public String getErrorPrefix() {
        return "SMALL_CATCH_COUNTS";
    }
}
