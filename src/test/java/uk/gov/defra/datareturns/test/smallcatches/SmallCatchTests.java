package uk.gov.defra.datareturns.test.smallcatches;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.test.activities.ActivityTests;
import uk.gov.defra.datareturns.test.submissions.SubmissionTests;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.violationMessageMatching;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@WithAdminUser
@Slf4j
public class SmallCatchTests {
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private MethodRepository methodRepository;
    @Inject
    private Validator validator;

    public static SmallCatch createSmallCatch(final Submission submission, final Activity activity, final List<SmallCatchCount> counts,
                                              final int released) {
        final SmallCatch cat = new SmallCatch();
        cat.setSubmission(submission);
        cat.setActivity(activity);
        cat.setMonth(Month.JANUARY);
        cat.setCounts(counts);
        cat.setReleased((short) released);
        return cat;
    }

    @Test
    public void testValidSmallCatch() {
        final SmallCatch cat = createValidSmallCatch();
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void testValidSmallCatchWithDefaultDateFlags() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setNoMonthRecorded(true);
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void testSmallCatchWithoutSubmissionFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setSubmission(null);
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_SUBMISSION_REQUIRED"));
    }

    @Test
    public void testSmallCatchWithoutActivityFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setActivity(null);
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_ACTIVITY_REQUIRED"));
    }

    @Test
    public void testSmallCatchWithoutMonthFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setMonth(null);
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_MONTH_REQUIRED"));
    }

    @Test
    public void testSmallCatchWithCurrentMonthSucceeds() {
        final SmallCatch cat = createValidSmallCatch();
        final YearMonth thisMonth = YearMonth.from(LocalDate.now());
        cat.getSubmission().setSeason((short) thisMonth.getYear());
        cat.setMonth(thisMonth.getMonth());
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(0);
    }

    @Test
    public void testSmallCatchWithFutureMonthFails() {
        final SmallCatch cat = createValidSmallCatch();
        final YearMonth nextMonth = YearMonth.from(LocalDate.now()).plusMonths(1);
        cat.getSubmission().setSeason((short) nextMonth.getYear());
        cat.setMonth(nextMonth.getMonth());
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_MONTH_IN_FUTURE"));
    }

    @Test
    public void testSmallCatchWithoutCountsFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setCounts(null);
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_COUNTS_REQUIRED"));
    }

    @Test
    public void testSmallCatchWithEmptyCountsFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.getCounts().clear();
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_COUNTS_REQUIRED"));
    }

    @Test
    public void testSmallCatchWithDuplicateMethodInCountsFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.getCounts().addAll(cat.getCounts());
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_COUNTS_METHOD_DUPLICATE_FOUND"));
    }


    @Test
    public void testSmallCatchWithoutReleasedFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setReleased(null);
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_RELEASED_REQUIRED"));
    }

    @Test
    public void testSmallCatchReleasedLessThanZeroFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setReleased((short) -1);
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_RELEASED_NEGATIVE"));
    }

    @Test
    public void testSmallCatchReleasedMoreThanCaughtFails() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setReleased((short) (cat.getCounts().stream().mapToInt(c -> (int) c.getCount()).sum() + 1));
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(violationMessageMatching("SMALL_CATCH_RELEASED_EXCEEDS_COUNTS"));
    }

    @Test
    public void testSmallCatchSetNoMonthRecorded() {
        final SmallCatch cat = createValidSmallCatch();
        cat.setNoMonthRecorded(true);
        final YearMonth thisMonth = YearMonth.from(LocalDate.now());
        cat.getSubmission().setSeason((short) thisMonth.getYear());
        cat.setMonth(thisMonth.getMonth());
        final Set<ConstraintViolation<SmallCatch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(0);
    }

    private SmallCatch createValidSmallCatch() {
        final Submission submission = SubmissionTests.createValidSubmission();
        final River river = riverRepository.getOne(RandomUtils.nextLong(1, 100));

        final Activity activity = ActivityTests.createValidActivity(submission, river, 1, 1);
        submission.setActivities(Collections.singletonList(activity));

        final List<SmallCatchCount> counts = new ArrayList<>();
        for (final Method method : methodRepository.findAll()) {
            final SmallCatchCount count = new SmallCatchCount();
            count.setCount((short) 1);
            count.setMethod(method);
            counts.add(count);
        }
        return createSmallCatch(submission, activity, counts, counts.size());
    }
}
