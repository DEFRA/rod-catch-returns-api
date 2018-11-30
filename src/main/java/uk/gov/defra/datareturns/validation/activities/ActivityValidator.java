package uk.gov.defra.datareturns.validation.activities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;

/**
 * Validate an {@link Activity} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class ActivityValidator extends AbstractConstraintValidator<ValidActivity, Activity> {
    @Override
    public void initialize(final ValidActivity constraintAnnotation) {
        super.addChecks(this::checkSubmission, this::checkRiver, this::checkRiverPermissions, this::checkUniqueRiverPerSubmission, this::checkDays);
    }

    /**
     * Check river is provided
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkRiver(final Activity activity, final ConstraintValidatorContext context) {
        return activity.getRiver() != null || handleError(context, "RIVER_REQUIRED", b -> b.addPropertyNode("river"));
    }

    /**
     * Check that the user has sufficient authority to use the given river
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkRiverPermissions(final Activity activity, final ConstraintValidatorContext context) {
        return checkRestrictedEntity(activity.getRiver(), "river", context);
    }


    /**
     * Check that a unique river is used in all activities
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkUniqueRiverPerSubmission(final Activity activity, final ConstraintValidatorContext context) {
        if (activity.getRiver() != null && activity.getSubmission() != null && activity.getSubmission().getActivities() != null) {
            final long riverCount = activity.getSubmission().getActivities().stream()
                    .filter(a -> activity != a && activity.getRiver().equals(a.getRiver()))
                    .count();
            if (riverCount > 0) {
                return handleError(context, "RIVER_DUPLICATE_FOUND", b -> b.addPropertyNode("river"));
            }

        }
        return true;
    }

    /**
     * Check that the count of days spent on the river is within possible maximums
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkDays(final Activity activity, final ConstraintValidatorContext context) {
        boolean valid = checkDaysMandatoryRelease(activity, context) && checkDaysOther(activity, context);
        if (valid) {
            if (activity.getDaysFishedWithMandatoryRelease() < 1 && activity.getDaysFishedOther() < 1) {
                valid = handleError(context, "DAYS_FISHED_NOT_GREATER_THAN_ZERO", ConstraintValidatorContext.ConstraintViolationBuilder::addBeanNode);
            }
        }
        return valid;
    }

    /**
     * Check that the count of days spent on the river while mandatory release imposed is defined and within range
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkDaysMandatoryRelease(final Activity activity, final ConstraintValidatorContext context) {
        final int maxDaysMandatory = activity.getSubmission() != null && activity.getSubmission().getSeason() % 4 == 0 ? 168 : 167;
        return checkDaysWithinLimit("DAYS_FISHED_WITH_MANDATORY_RELEASE", "daysFishedWithMandatoryRelease",
                maxDaysMandatory, activity.getDaysFishedWithMandatoryRelease(), context);
    }

    /**
     * Check that the count of days spent on the river at other times is defined and within range
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkDaysOther(final Activity activity, final ConstraintValidatorContext context) {
        return checkDaysWithinLimit("DAYS_FISHED_OTHER", "daysFishedOther", 198, activity.getDaysFishedOther(), context);
    }

    private boolean checkDaysWithinLimit(final String errorPrefix, final String errorField, final int maxAllowedDays, final Short actualValue,
                                         final ConstraintValidatorContext context) {
        boolean valid = true;
        if (actualValue == null) {
            valid = handleError(context, errorPrefix + "_REQUIRED", b -> b.addPropertyNode(errorField));
        } else if (actualValue < 0) {
            valid = handleError(context, errorPrefix + "_NEGATIVE", b -> b.addPropertyNode(errorField));
        } else if (actualValue > maxAllowedDays) {
            valid = handleError(context, errorPrefix + "_MAX_EXCEEDED", b -> b.addPropertyNode(errorField));
        }
        return valid;
    }


    @Override
    public String getErrorPrefix() {
        return "ACTIVITY";
    }
}
