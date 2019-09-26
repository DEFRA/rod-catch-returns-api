package uk.gov.defra.datareturns.validation.smallcatches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validate a {@link uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class SmallCatchValidator extends AbstractConstraintValidator<ValidSmallCatch, SmallCatch> {
    private static final String PROPERTY_COUNTS = "counts";
    private static final String PROPERTY_RELEASED = "released";
    private static final String PROPERTY_MONTH = "month";
    private static final String PROPERTY_ACTIVITY = "activity";


    @Override
    public void initialize(final ValidSmallCatch constraintAnnotation) {
        super.addChecks(
                this::checkActivity, this::checkMonth,
                this::checkUniqueActivityAndMonth, this::checkCountsProvided, this::checkCountMethodDuplicates,
                this::checkReleased, this::checkReleasedDoesNotExceedCounts);
    }

    /**
     * Check activity is provided
     *
     * @param smallCatch the {@link SmallCatch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkActivity(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        return smallCatch.getActivity() != null || handleError(context, "ACTIVITY_REQUIRED", PROPERTY_ACTIVITY);
    }

    /**
     * Check month is provided
     *
     * @param smallCatch the {@link SmallCatch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMonth(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        final LocalDate today = LocalDate.now();
        boolean valid = true;
        if (smallCatch.getMonth() == null) {
            if (smallCatch.isNoMonthRecorded()) {
                valid = handleError(context, "DEFAULT_MONTH_REQUIRED", PROPERTY_MONTH);
            } else {
                valid = handleError(context, "MONTH_REQUIRED", PROPERTY_MONTH);
            }
        } else if (smallCatch.getMonth().getValue() > Month.from(today).getValue()
                && smallCatch.getActivity().getSubmission().getSeason() >= today.getYear()) {
            valid = handleError(context, "MONTH_IN_FUTURE", PROPERTY_MONTH);
        }
        return valid;
    }

    /**
     * Check counts have been provided
     *
     * @param smallCatch the {@link SmallCatch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkCountsProvided(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        return CollectionUtils.isNotEmpty(smallCatch.getCounts()) || handleError(context, "COUNTS_REQUIRED", PROPERTY_COUNTS);
    }

    /**
     * Check that there are no duplicate small catches with the same activity and month
     *
     * @param smallCatch the {@link SmallCatch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkUniqueActivityAndMonth(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        boolean valid = true;
        if (smallCatch.getActivity() != null && smallCatch.getMonth() != null && smallCatch.getActivity().getSubmission() != null
                && smallCatch.getActivity().getSmallCatches() != null) {
            for (int i = 0; i < smallCatch.getActivity().getSmallCatches().size(); i++) {
                final SmallCatch other = smallCatch.getActivity().getSmallCatches().get(i);
                if (smallCatch != other && smallCatch.getActivity().equals(other.getActivity()) && smallCatch.getMonth().equals(other.getMonth())) {
                    valid = handleError(context, "DUPLICATE_FOUND", ConstraintValidatorContext.ConstraintViolationBuilder::addBeanNode);
                }
            }
        }
        return valid;
    }


    /**
     * Check that there are no duplicate counts for the same method
     *
     * @param smallCatch the {@link SmallCatch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkCountMethodDuplicates(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        if (CollectionUtils.isNotEmpty(smallCatch.getCounts())) {
            final List<Method> allMethodsUsed = smallCatch.getCounts().stream().map(SmallCatchCount::getMethod).collect(Collectors.toList());
            if (allMethodsUsed.size() != new HashSet<>(allMethodsUsed).size()) {
                return handleError(context, "COUNTS_METHOD_DUPLICATE_FOUND", PROPERTY_COUNTS);
            }
        }
        return true;
    }

    /**
     * Check that the released value has been provided and is not negative
     *
     * @param smallCatch the {@link SmallCatch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkReleased(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        if (smallCatch.getReleased() == null) {
            return handleError(context, "RELEASED_REQUIRED", PROPERTY_RELEASED);
        }
        if (smallCatch.getReleased() < 0) {
            return handleError(context, "RELEASED_NEGATIVE", PROPERTY_RELEASED);
        }
        return true;
    }

    /**
     * Check that the released value does not exceed the sum of the fish caught
     *
     * @param smallCatch the {@link SmallCatch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkReleasedDoesNotExceedCounts(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        if (CollectionUtils.isNotEmpty(smallCatch.getCounts()) && smallCatch.getReleased() != null) {
            boolean checkReleasedExceedsCounts = true;
            int totalCaught = 0;

            for (final SmallCatchCount c : smallCatch.getCounts()) {
                if (c.getCount() == null) {
                    checkReleasedExceedsCounts = false;
                    break;
                }
                totalCaught += c.getCount();
            }
            if (checkReleasedExceedsCounts && smallCatch.getReleased() > totalCaught) {
                return handleError(context, "RELEASED_EXCEEDS_COUNTS", PROPERTY_RELEASED);
            }
        }
        return true;
    }


    @Override
    public String getErrorPrefix() {
        return "SMALL_CATCH";
    }
}
