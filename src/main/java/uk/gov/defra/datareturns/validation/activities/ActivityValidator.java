package uk.gov.defra.datareturns.validation.activities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.activities.Activity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static uk.gov.defra.datareturns.validation.util.ValidationUtil.handleError;

/**
 * Validate an {@link Activity} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class ActivityValidator implements ConstraintValidator<ValidActivity, Activity> {
    @Override
    public void initialize(final ValidActivity constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Activity activity, final ConstraintValidatorContext context) {
        boolean valid = checkRiver(activity, context);
        valid = checkDays(activity, context) && valid;
        return valid;
    }

    private boolean checkRiver(final Activity activity, final ConstraintValidatorContext context) {
        if (activity.getRiver() == null) {
            return handleError(context, "ACTIVITY_RIVER_REQUIRED", b -> b.addPropertyNode("river"));
        }
        return true;
    }

    private boolean checkDays(final Activity activity, final ConstraintValidatorContext context) {
        final int maxDays = activity.getSubmission().getSeason() % 4 == 0 ? 366 : 365;

        if (activity.getDays() < 1) {
            return handleError(context, "ACTIVITY_DAYS_NOT_GREATER_THAN_ZERO", b -> b.addPropertyNode("days"));
        } else if (activity.getDays() > maxDays) {
            return handleError(context, "ACTIVITY_DAYS_MAX_EXCEEDED", b -> b.addPropertyNode("days"));

        }
        return true;
    }
}
