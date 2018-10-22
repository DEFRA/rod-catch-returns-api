package uk.gov.defra.datareturns.test.smallcatches;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.SubmissionTestUtils;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@WithAdminUser
@Slf4j
public class SmallCatchCountTests {
    @Inject
    private MethodRepository methodRepository;
    @Inject
    private Validator validator;

    public static SmallCatchCount createValidSmallCatchCount(final Method method, final int days) {
        final SmallCatchCount count = new SmallCatchCount();
        count.setMethod(method);
        count.setCount((short) days);
        return count;
    }

    @Test
    public void testValidSmallCatchCount() {
        final SmallCatchCount cat = createValidSmallCatchCount(methodRepository.getOne(1L), 1);
        final Set<ConstraintViolation<SmallCatchCount>> violations = validator.validate(cat);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void testValidSmallCatchWithoutMethodFails() {
        final SmallCatchCount cat = createValidSmallCatchCount(null, 1);
        final Set<ConstraintViolation<SmallCatchCount>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, SubmissionTestUtils.violationMessageMatching("SMALL_CATCH_COUNTS_METHOD_REQUIRED"));
    }

    @Test
    public void testValidSmallCatchCountNegativeFails() {
        final SmallCatchCount cat = createValidSmallCatchCount(methodRepository.getOne(1L), -1);
        final Set<ConstraintViolation<SmallCatchCount>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, SubmissionTestUtils.violationMessageMatching("SMALL_CATCH_COUNTS_NOT_GREATER_THAN_ZERO"));
    }
}
