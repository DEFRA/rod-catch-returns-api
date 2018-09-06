package uk.gov.defra.datareturns.validation.smallcatches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.validation.ValidationChecks;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.defra.datareturns.validation.util.ValidationUtil.handleError;

/**
 * Validate a {@link uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class SmallCatchValidator implements ConstraintValidator<ValidSmallCatch, SmallCatch> {
    @Override
    public void initialize(final ValidSmallCatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(final SmallCatch catchEntry, final ConstraintValidatorContext context) {
        boolean valid = checkRiver(catchEntry, context);
        valid = checkMonth(catchEntry, context) && valid;
        valid = checkMethods(catchEntry, context) && valid;
        valid = checkTotals(catchEntry, context) && valid;
        return valid;
    }

    private boolean checkRiver(final SmallCatch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getRiver() == null) {
            return handleError(context, "SMALL_CATCH_RIVER_REQUIRED", b -> b.addPropertyNode("river"));
        }
        if (!ValidationChecks.checkRiverDefinedInActivities(catchEntry.getSubmission(), catchEntry.getRiver())) {
            return handleError(context, "SMALL_CATCH_RIVER_NOT_DEFINED_IN_ACTIVITIES", b -> b.addPropertyNode("river"));
        }
        return true;
    }

    private boolean checkMonth(final SmallCatch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getMonth() == null) {
            return handleError(context, "SMALL_CATCH_MONTH_REQUIRED", b -> b.addPropertyNode("month"));
        }
        return true;
    }

    private boolean checkMethods(final SmallCatch catchEntry, final ConstraintValidatorContext context) {
        if (catchEntry.getCounts() != null) {
            final List<Method> allMethodsUsed = catchEntry.getCounts().stream().map(SmallCatchCount::getMethod)
                    .collect(Collectors.toList());
            final Set<Method> uniqueMethods = new HashSet<>(allMethodsUsed);
            if (allMethodsUsed.size() != uniqueMethods.size()) {
                return handleError(context, "SMALL_CATCH_DUPLICATE_METHOD_IN_COUNTS", b -> b.addPropertyNode("totals"));
            }
        }
        return true;
    }

    private boolean checkTotals(final SmallCatch catchEntry, final ConstraintValidatorContext context) {
        int totalCount = 0;
        if (catchEntry.getCounts() != null) {
            for (final SmallCatchCount catchByMethod : catchEntry.getCounts()) {
                if (catchByMethod.getCount() < 0) {
                    return handleError(context, "SMALL_CATCH_COUNT_NOT_GREATER_THAN_ZERO", b -> b.addPropertyNode("totals"));
                }
                totalCount += catchByMethod.getCount();
            }
        }
        if (catchEntry.getReleased() < 0) {
            return handleError(context, "SMALL_CATCH_RELEASED_NOT_GREATER_THAN_ZERO", b -> b.addPropertyNode("released"));
        }

        if (catchEntry.getReleased() > totalCount) {
            return handleError(context, "SMALL_CATCH_RELEASED_MORE_THAN_CAUGHT", b -> b.addPropertyNode("released"));
        }
        return true;
    }
}
