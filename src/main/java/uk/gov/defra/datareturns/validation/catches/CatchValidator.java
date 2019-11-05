package uk.gov.defra.datareturns.validation.catches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

/**
 * Validate a {@link Catch} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class CatchValidator extends AbstractConstraintValidator<ValidCatch, Catch> {
    private static final String PROPERTY_DATE_CAUGHT = "dateCaught";
    private static final String PROPERTY_MASS = "mass";
    private static final String PROPERTY_METHOD = "method";
    private static final String PROPERTY_RELEASED = "released";
    private static final String PROPERTY_SPECIES = "species";
    private static final String PROPERTY_ACTIVITY = "activity";
    private static final String PROPERTY_ONLY_MONTH = "onlyMonthRecorded";

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
        super.addChecks(this::checkActivity, this::checkDate, this::checkSpecies,
                this::checkMass, this::checkMassValue, this::checkMassLimits, this::checkMethod,
                this::checkMethodPermissions, this::checkReleased, this::checkDefaultDateFlagConflict);
    }

    /**
     * Check activity is provided
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkActivity(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getActivity() != null || handleError(context, "ACTIVITY_REQUIRED", PROPERTY_ACTIVITY);
    }

    /**
     * Check date caught is provided and valid with respect to the the submission year and the current date
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkDate(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getDateCaught() == null) {
            if (catchEntry.isNoDateRecorded() || catchEntry.isOnlyMonthRecorded()) {
                return handleError(context, "DEFAULT_DATE_REQUIRED", PROPERTY_DATE_CAUGHT);
            } else {
                return handleError(context, "DATE_REQUIRED", PROPERTY_DATE_CAUGHT);
            }
        }
        if (catchEntry.getActivity() != null && catchEntry.getActivity().getSubmission() != null) {
            final int yearCaught = DateUtils.toCalendar(catchEntry.getDateCaught()).get(Calendar.YEAR);
            if (yearCaught != catchEntry.getActivity().getSubmission().getSeason().intValue()) {
                return handleError(context, "YEAR_MISMATCH", PROPERTY_DATE_CAUGHT);
            }
        }

        final LocalDate dateCaught = Instant.ofEpochMilli(catchEntry.getDateCaught().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        if (dateCaught.isAfter(LocalDate.now())) {
            return handleError(context, "DATE_IN_FUTURE", PROPERTY_DATE_CAUGHT);
        }
        return true;
    }

    /**
     * Check that the only month flag is not set when the no date recorded flag is set. It is superfluous
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkDefaultDateFlagConflict(final Catch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.isNoDateRecorded() && catchEntry.isOnlyMonthRecorded()) {
            return handleError(context, "NO_DATE_RECORDED_WITH_ONLY_MONTH_RECORDED", PROPERTY_ONLY_MONTH);
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
        return catchEntry.getSpecies() != null || handleError(context, "SPECIES_REQUIRED", PROPERTY_SPECIES);
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
            return handleError(context, "MASS_REQUIRED", PROPERTY_MASS);
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
                    return handleError(context, "MASS_BELOW_MINIMUM", PROPERTY_MASS);
                } else if (MAX_FISH_MASS_KG.compareTo(catchEntry.getMass().getKg()) < 1) {
                    return handleError(context, "MASS_MAX_EXCEEDED", PROPERTY_MASS);
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
        return catchEntry.getMethod() != null || handleError(context, "METHOD_REQUIRED", PROPERTY_METHOD);
    }

    /**
     * Check that the user has sufficient authority to use the given method
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkMethodPermissions(final Catch catchEntry, final ConstraintValidatorContext context) {
        return checkRestrictedEntity(catchEntry.getMethod(), PROPERTY_METHOD, context);
    }


    /**
     * Check that the released property has been provided
     *
     * @param catchEntry the {@link Catch} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkReleased(final Catch catchEntry, final ConstraintValidatorContext context) {
        return catchEntry.getReleased() != null || handleError(context, "RELEASED_REQUIRED", PROPERTY_RELEASED);
    }


    @Override
    public String getErrorPrefix() {
        return "CATCH";
    }
}
