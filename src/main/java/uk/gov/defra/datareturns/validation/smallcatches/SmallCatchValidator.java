package uk.gov.defra.datareturns.validation.smallcatches;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validate a {@link uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class SmallCatchValidator extends AbstractConstraintValidator<ValidSmallCatch, SmallCatch> {
    @Override
    public void initialize(final ValidSmallCatch constraintAnnotation) {
        super.addChecks(
                this::checkSubmission, this::checkActivity, this::checkMonth,
                this::checkUniqueActivityAndMonth, this::checkCountsProvided, this::checkCountMethodDuplicates, this::checkReleased);
    }

    private boolean checkActivity(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        return smallCatch.getActivity() != null || handleError(context, "ACTIVITY_REQUIRED", b -> b.addPropertyNode("activity"));
    }

    private boolean checkMonth(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        return smallCatch.getMonth() != null || handleError(context, "MONTH_REQUIRED", b -> b.addPropertyNode("month"));
    }

    private boolean checkCountsProvided(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        return CollectionUtils.isNotEmpty(smallCatch.getCounts()) || handleError(context, "COUNTS_REQUIRED", b -> b.addPropertyNode("counts"));
    }

    private boolean checkUniqueActivityAndMonth(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        boolean valid = true;
        if (smallCatch.getActivity() != null && smallCatch.getMonth() != null && smallCatch.getSubmission() != null
                && smallCatch.getSubmission().getSmallCatches() != null) {
            for (int i = 0; i < smallCatch.getSubmission().getSmallCatches().size(); i++) {
                final SmallCatch other = smallCatch.getSubmission().getSmallCatches().get(i);
                if (smallCatch != other && smallCatch.getActivity().equals(other.getActivity()) && smallCatch.getMonth().equals(other.getMonth())) {
                    valid = handleError(context, "DUPLICATE_FOUND", ConstraintValidatorContext.ConstraintViolationBuilder::addBeanNode);
                }
            }
        }
        return valid;
    }


    private boolean checkCountMethodDuplicates(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        if (CollectionUtils.isNotEmpty(smallCatch.getCounts())) {
            final List<Method> allMethodsUsed = smallCatch.getCounts().stream().map(SmallCatchCount::getMethod).collect(Collectors.toList());
            if (allMethodsUsed.size() != new HashSet<>(allMethodsUsed).size()) {
                return handleError(context, "COUNTS_METHOD_DUPLICATE_FOUND", b -> b.addPropertyNode("counts"));
            }
        }
        return true;
    }

    private boolean checkReleased(final SmallCatch smallCatch, final ConstraintValidatorContext context) {
        if (smallCatch.getReleased() < 0) {
            return handleError(context, "RELEASED_NEGATIVE", b -> b.addPropertyNode("released"));
        }

        if (CollectionUtils.isNotEmpty(smallCatch.getCounts())) {
            final int total = smallCatch.getCounts().stream().mapToInt(SmallCatchCount::getCount).sum();
            if (smallCatch.getReleased() > total) {
                return handleError(context, "RELEASED_EXCEEDS_COUNTS", b -> b.addPropertyNode("released"));
            }
        }
        return true;
    }

    @Override
    public String getErrorPrefix() {
        return "SMALL_CATCH";
    }
}
