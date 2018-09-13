package uk.gov.defra.datareturns.validation.catches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Validate a {@link Catch} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class CatchValidator extends AbstractConstraintValidator<ValidCatch, Catch> {
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
        super.addChecks(this::checkSubmission, this::checkActivity, this::checkDate, this::checkType, this::checkMassType, this::checkMass,
                this::checkMethod);
    }

    private boolean checkActivity(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getActivity() != null || handleError(context, "ACTIVITY_REQUIRED", b -> b.addPropertyNode("activity"));
    }

    private boolean checkDate(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getDateCaught() == null) {
            return handleError(context, "DATE_REQUIRED", b -> b.addPropertyNode("dateCaught"));
        }
        if (catchEntry.getSubmission() != null) {
            final int yearCaught = DateUtils.toCalendar(catchEntry.getDateCaught()).get(Calendar.YEAR);
            if (yearCaught != catchEntry.getSubmission().getSeason().intValue()) {
                return handleError(context, "YEAR_MISMATCH", b -> b.addPropertyNode("dateCaught"));
            }
        }
        return true;
    }

    private boolean checkType(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getSpecies() != null || handleError(context, "SPECIES_REQUIRED", b -> b.addPropertyNode("species"));
    }

    private boolean checkMassType(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getMass().getType() != null || handleError(context, "MASS_TYPE_REQUIRED", b -> b.addPropertyNode("mass"));
    }

    private boolean checkMass(final Catch catchEntry, final ConstraintValidatorContext context) {
        // Ensure that the mass has been conciliated before attempting to validate based on the metric value
        catchEntry.getMass().conciliateMass();

        if (MIN_FISH_MASS_KG.compareTo(catchEntry.getMass().getKg()) > -1) {
            return handleError(context, "MASS_BELOW_MINIMUM", b -> b.addPropertyNode("mass"));
        } else if (MAX_FISH_MASS_KG.compareTo(catchEntry.getMass().getKg()) < 1) {
            return handleError(context, "MASS_MAX_EXCEEDED", b -> b.addPropertyNode("mass"));
        }
        return true;
    }

    private boolean checkMethod(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getMethod() != null || handleError(context, "METHOD_REQUIRED", b -> b.addPropertyNode("method"));
    }

    @Override
    public String getErrorPrefix() {
        return "CATCH";
    }
}
