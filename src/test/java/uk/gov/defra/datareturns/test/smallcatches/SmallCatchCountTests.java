package uk.gov.defra.datareturns.test.smallcatches;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;
import uk.gov.defra.datareturns.testutils.SubmissionTestUtils;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@WebIntegrationTest
@Slf4j
public class SmallCatchCountTests {
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private MethodRepository methodRepository;
    @Inject
    private Validator validator;

    @Test
    public void testValidSmallCatchCount() {
        final SmallCatchCount cat = createValidSmallCatchCount();
        final Set<ConstraintViolation<SmallCatchCount>> violations = validator.validate(cat);
        Assertions.assertThat(violations).isEmpty();
    }


    @Test
    public void testValidSmallCatchWithoutMethodFails() {
        final SmallCatchCount cat = createValidSmallCatchCount();
        cat.setMethod(null);
        final Set<ConstraintViolation<SmallCatchCount>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1)
                .haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("SMALL_CATCH_COUNTS_METHOD_REQUIRED"));
    }

    @Test
    public void testValidSmallCatchCountNegativeFails() {
        final SmallCatchCount cat = createValidSmallCatchCount();
        cat.setCount((short) -1);
        final Set<ConstraintViolation<SmallCatchCount>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1)
                .haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("SMALL_CATCH_COUNTS_NOT_GREATER_THAN_ZERO"));
    }

    private SmallCatchCount createValidSmallCatchCount() {
        final SmallCatchCount count = new SmallCatchCount();
        count.setMethod(methodRepository.getOne(1L));
        count.setCount((short) 1);
        return count;
    }
}
