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
        boolean valid = true;
        if (activity.getDaysFishedWithMandatoryRelease() == null) {
            valid = handleError(context, "DAYS_FISHED_WITH_MANDATORY_RELEASE_REQUIRED", b -> b.addPropertyNode("daysFishedWithMandatoryRelease"));
        } else if (activity.getDaysFishedWithMandatoryRelease() < 0) {
            valid = handleError(context, "DAYS_FISHED_WITH_MANDATORY_RELEASE_NEGATIVE", b -> b.addPropertyNode("daysFishedWithMandatoryRelease"));
        } else if (activity.getDaysFishedWithMandatoryRelease() > maxDaysMandatory) {
            valid = handleError(context, "DAYS_FISHED_WITH_MANDATORY_RELEASE_MAX_EXCEEDED", b -> b.addPropertyNode("daysFishedWithMandatoryRelease"));
        }
        return valid;
    }

    /**
     * Check that the count of days spent on the river at other times is defined and within range
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkDaysOther(final Activity activity, final ConstraintValidatorContext context) {
        final int maxDaysOther = 198;
        boolean valid = true;

        if (activity.getDaysFishedOther() == null) {
            valid = handleError(context, "DAYS_FISHED_OTHER_REQUIRED", b -> b.addPropertyNode("daysFishedOther"));
        } else if (activity.getDaysFishedOther() < 0) {
            valid = handleError(context, "DAYS_FISHED_OTHER_NEGATIVE", b -> b.addPropertyNode("daysFishedOther"));
        } else if (activity.getDaysFishedOther() > maxDaysOther) {
            valid = handleError(context, "DAYS_FISHED_OTHER_MAX_EXCEEDED", b -> b.addPropertyNode("daysFishedOther"));
        }
        return valid;
    }


    @Override
    public String getErrorPrefix() {
        return "ACTIVITY";
    }
}
