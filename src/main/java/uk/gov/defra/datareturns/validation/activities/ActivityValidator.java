package uk.gov.defra.datareturns.validation.activities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionSource;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validate an {@link Activity} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class ActivityValidator extends AbstractConstraintValidator<ValidActivity, Activity> {
    private static final String PROPERTY_RIVER = "river";
    private static final String PROPERTY_DAYS_FISHED_WITH_MANDATORY_RELEASE = "daysFishedWithMandatoryRelease";
    private static final String PROPERTY_DAYS_FISHED_OTHER = "daysFishedOther";

    @Override
    public void initialize(final ValidActivity constraintAnnotation) {
        super.addChecks(this::checkSubmission, this::checkRiver, this::checkRiverPermissions, this::checkUniqueRiverPerSubmission, this::checkDays);
    }

    /**
     * Checks the activity's reference to its submission is not null
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if the object's reference to its submission is not null, false otherwise
     */
    public boolean checkSubmission(final Activity activity, final ConstraintValidatorContext context) {
        return activity.getSubmission() != null || handleError(context, "SUBMISSION_REQUIRED", "submission");
    }

    /**
     * Check river is provided
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkRiver(final Activity activity, final ConstraintValidatorContext context) {
        return activity.getRiver() != null || handleError(context, "RIVER_REQUIRED", PROPERTY_RIVER);
    }

    /**
     * Check that the user has sufficient authority to use the given river
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkRiverPermissions(final Activity activity, final ConstraintValidatorContext context) {
        return checkRestrictedEntity(activity.getRiver(), PROPERTY_RIVER, context);
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
                return handleError(context, "RIVER_DUPLICATE_FOUND", PROPERTY_RIVER);
            }

        }
        return true;
    }

    /**
     * Check that the count of days spent on the river is within possible maximums.
     * Zero is permitted for the FMT user (Paper submissions)
     *
     * @param activity the activity to be validated
     * @param context  the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkDays(final Activity activity, final ConstraintValidatorContext context) {
        boolean valid = checkDaysMandatoryRelease(activity, context) && checkDaysOther(activity, context);

        final Optional<Submission> sub = Optional.ofNullable(activity.getSubmission());

        if (sub.isPresent()) {
            if (valid && sub.get().getSource().equals(SubmissionSource.WEB)
                    && activity.getDaysFishedWithMandatoryRelease() < 1 && activity.getDaysFishedOther() < 1) {
                valid = handleError(context, "DAYS_FISHED_NOT_GREATER_THAN_ZERO", ConstraintValidatorContext.ConstraintViolationBuilder::addBeanNode);
            }

            if (valid && sub.get().getSource().equals(SubmissionSource.PAPER)
                    && activity.getDaysFishedWithMandatoryRelease() < 0 && activity.getDaysFishedOther() < 0) {
                valid = handleError(context, "DAYS_FISHED_LESS_THAN_ZERO", ConstraintValidatorContext.ConstraintViolationBuilder::addBeanNode);
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
        return checkDaysWithinLimit("DAYS_FISHED_WITH_MANDATORY_RELEASE", PROPERTY_DAYS_FISHED_WITH_MANDATORY_RELEASE,
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
        return checkDaysWithinLimit("DAYS_FISHED_OTHER", PROPERTY_DAYS_FISHED_OTHER, 198, activity.getDaysFishedOther(), context);
    }

    private boolean checkDaysWithinLimit(final String errorPrefix, final String errorField, final int maxAllowedDays, final Short actualValue,
                                         final ConstraintValidatorContext context) {
        boolean valid = true;
        if (actualValue == null) {
            valid = handleError(context, errorPrefix + "_REQUIRED", errorField);
        } else if (actualValue < 0) {
            valid = handleError(context, errorPrefix + "_NEGATIVE", errorField);
        } else if (actualValue > maxAllowedDays) {
            valid = handleError(context, errorPrefix + "_MAX_EXCEEDED", errorField);
        }
        return valid;
    }


    @Override
    public String getErrorPrefix() {
        return "ACTIVITY";
    }
}
