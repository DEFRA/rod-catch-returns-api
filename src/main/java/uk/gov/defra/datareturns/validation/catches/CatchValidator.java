package uk.gov.defra.datareturns.validation.catches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.rivers.River;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.defra.datareturns.validation.util.ValidationUtil.handleError;

/**
 * Validate a {@link Catch} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class CatchValidator implements ConstraintValidator<ValidCatch, Catch> {
    /**
     * Maximum possible mass of a salmon/sea trout (world record is about 48kg)
     */
    private static final BigDecimal MAX_FISH_MASS_KG = BigDecimal.valueOf(50);

    /**
     * Anything under this threshold should be recorded as a small catch return
     */
    private static final BigDecimal MIN_FISH_MASS_KG = BigDecimal.valueOf(0.453592);



    @Override
    public void initialize(final ValidCatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Catch catchEntry, final ConstraintValidatorContext context) {
        boolean valid = checkRiver(catchEntry, context);
        valid = checkDate(catchEntry, context) && valid;
        valid = checkType(catchEntry, context) && valid;
        valid = checkMass(catchEntry, context) && valid;
        valid = checkMethod(catchEntry, context) && valid;
        return valid;
    }

    private boolean checkRiver(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getRiver() == null) {
            return handleError(context, "CATCH_RIVER_REQUIRED", b -> b.addPropertyNode("river"));
        }
        if (catchEntry.getSubmission() != null && catchEntry.getSubmission().getSubmissionActivities() != null) {
            final Set<River> allowedRivers = catchEntry.getSubmission().getSubmissionActivities().stream()
                    .map(Activity::getRiver).collect(Collectors.toSet());

            if (!allowedRivers.contains(catchEntry.getRiver())) {
                return handleError(context, "CATCH_RIVER_NOT_DEFINED_IN_ACTIVITIES", b -> b.addPropertyNode("river"));
            }
        }
        return true;
    }

    private boolean checkDate(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getDateCaught() == null) {
            return handleError(context, "CATCH_DATE_REQUIRED", b -> b.addPropertyNode("dateCaught"));
        }
        final int yearCaught = DateUtils.toCalendar(catchEntry.getDateCaught()).get(Calendar.YEAR);
        if (yearCaught != catchEntry.getSubmission().getSeason().intValue()) {
            return handleError(context, "CATCH_YEAR_MISMATCH", b -> b.addPropertyNode("dateCaught"));
        }
        return true;
    }

    private boolean checkType(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getSpecies() == null) {
            return handleError(context, "CATCH_SPECIES_REQUIRED", b -> b.addPropertyNode("species"));
        }
        return true;
    }

    private boolean checkMass(final Catch catchEntry, final ConstraintValidatorContext context) {
        // Ensure that the mass has been conciliated before attempting to validate based on the metric value
        catchEntry.getMass().conciliateMass();

        if (MIN_FISH_MASS_KG.compareTo(catchEntry.getMass().getKg()) > -1) {
            return handleError(context, "CATCH_MASS_BELOW_MINIMUM", b -> b.addPropertyNode("mass"));
        } else if (MAX_FISH_MASS_KG.compareTo(catchEntry.getMass().getKg()) < 1) {
            return handleError(context, "CATCH_MASS_MAX_EXCEEDED", b -> b.addPropertyNode("mass"));
        }
        return true;
    }

    private boolean checkMethod(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getMethod() == null) {
            return handleError(context, "CATCH_METHOD_REQUIRED", b -> b.addPropertyNode("method"));
        }
        return true;
    }
}
