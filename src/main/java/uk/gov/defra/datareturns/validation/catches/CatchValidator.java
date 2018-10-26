package uk.gov.defra.datareturns.validation.catches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
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
    private static final BigDecimal MIN_FISH_MASS_KG = BigDecimal.valueOf(0);

    @Override
    public void initialize(final ValidCatch constraintAnnotation) {
        super.addChecks(this::checkSubmission, this::checkActivity, this::checkDate, this::checkSpecies,
                this::checkMass, this::checkMassValue, this::checkMassLimits, this::checkMethod);
    }

    /**
     * Check activity is provided
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkActivity(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getActivity() != null || handleError(context, "ACTIVITY_REQUIRED", b -> b.addPropertyNode("activity"));
    }

    /**
     * Check date caught is provided and valid with respect to the the submission year
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
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

    /**
     * Check species is provided
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkSpecies(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getSpecies() != null || handleError(context, "SPECIES_REQUIRED", b -> b.addPropertyNode("species"));
    }

    /**
     * Check mass is provided and its type is properly set
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMass(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getMass() == null) {
            return handleError(context, "MASS_REQUIRED", b -> b.addPropertyNode("mass"));
        }
        return catchEntry.getMass().getType() != null || handleError(context, "MASS_TYPE_REQUIRED",
                b -> b.addPropertyNode("mass").addPropertyNode("type"));
    }

    /**
     * Check mass value is provided with respect to the mass type that is set
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMassValue(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getMass() != null) {
            if (CatchMass.MeasurementType.IMPERIAL.equals(catchEntry.getMass().getType())) {
                return catchEntry.getMass().getOz() != null || handleError(context, "MASS_OZ_REQUIRED",
                        b -> b.addPropertyNode("mass").addPropertyNode("oz"));
            } else if (CatchMass.MeasurementType.METRIC.equals(catchEntry.getMass().getType())) {
                return catchEntry.getMass().getKg() != null || handleError(context, "MASS_KG_REQUIRED",
                        b -> b.addPropertyNode("mass").addPropertyNode("kg"));
            }
        }
        return true;
    }

    /**
     * Check mass values are within limits
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMassLimits(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getMass() != null) {
            // Ensure that the mass has been conciliated before attempting to validate based on the metric value
            catchEntry.getMass().conciliateMass();

            if (catchEntry.getMass().getKg() != null) {
                if (MIN_FISH_MASS_KG.compareTo(catchEntry.getMass().getKg()) > -1) {
                    return handleError(context, "MASS_BELOW_MINIMUM", b -> b.addPropertyNode("mass"));
                } else if (MAX_FISH_MASS_KG.compareTo(catchEntry.getMass().getKg()) < 1) {
                    return handleError(context, "MASS_MAX_EXCEEDED", b -> b.addPropertyNode("mass"));
                }
            }
        }
        return true;
    }

    /**
     * Check that a method has been provided
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMethod(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getMethod() != null || handleError(context, "METHOD_REQUIRED", b -> b.addPropertyNode("method"));
    }

    @Override
    public String getErrorPrefix() {
        return "CATCH";
    }
}
