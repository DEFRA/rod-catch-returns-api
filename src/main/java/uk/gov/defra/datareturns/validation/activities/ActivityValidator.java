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
        super.addChecks(this::checkSubmission, this::checkRiver, this::checkUniqueRiverPerSubmission, this::checkDays);
    }

    private boolean checkRiver(final Activity activity, final ConstraintValidatorContext context) {
        return activity.getRiver() != null || handleError(context, "RIVER_REQUIRED", b -> b.addPropertyNode("river"));
    }

    private boolean checkUniqueRiverPerSubmission(final Activity activity, final ConstraintValidatorContext context) {
        if (activity.getRiver() != null && activity.getSubmission() != null && activity.getSubmission().getActivities() != null) {
            final long riverCount = activity.getSubmission().getActivities().stream().filter(a -> activity.getRiver().equals(a.getRiver())).count();
            if (riverCount > 0) {
                return handleError(context, "RIVER_DUPLICATE_FOUND", b -> b.addPropertyNode("river"));
            }

        }
        return true;
    }


    private boolean checkDays(final Activity activity, final ConstraintValidatorContext context) {
        final int maxDays = activity.getSubmission() != null && activity.getSubmission().getSeason() % 4 == 0 ? 366 : 365;

        if (activity.getDays() < 1) {
            return handleError(context, "DAYS_NOT_GREATER_THAN_ZERO", b -> b.addPropertyNode("days"));
        } else if (activity.getDays() > maxDays) {
            return handleError(context, "DAYS_MAX_EXCEEDED", b -> b.addPropertyNode("days"));

        }
        return true;
    }

    @Override
    public String getErrorPrefix() {
        return "ACTIVITY";
    }
}
