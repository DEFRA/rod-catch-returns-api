package uk.gov.defra.datareturns.test.submissions;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionStatus;
import uk.gov.defra.datareturns.test.activities.ActivityTests;
import uk.gov.defra.datareturns.test.smallcatches.SmallCatchCountTests;
import uk.gov.defra.datareturns.test.smallcatches.SmallCatchTests;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;
import uk.gov.defra.datareturns.testutils.SubmissionTestUtils;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Integration tests submission-level property validation
 */
@RunWith(SpringRunner.class)
@WebIntegrationTest
@Slf4j
public class SubmissionTests {
    @Inject
    private MethodRepository methodRepository;
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private Validator validator;

    public static Submission createValidSubmission() {
        final Submission sub = new Submission();
        sub.setContactId("123");
        sub.setSeason(getYear(0));
        sub.setStatus(SubmissionStatus.INCOMPLETE);
        return sub;
    }

    /**
     * Return the year with the given offset from the current year computed from the system clock
     *
     * @param offset the offset to use (e.g. -1 for the previous year)
     * @return the year value calculated using the given offset
     */
    private static short getYear(final int offset) {
        return Integer.valueOf(Calendar.getInstance().get(Calendar.YEAR) + offset).shortValue();
    }

    @Test
    public void testSubmissionYear() {
        final Submission sub = createValidSubmission();
        sub.setSeason(getYear(-1));
        final Set<ConstraintViolation<Submission>> violations = validator.validate(sub);
        Assertions.assertThat(violations).isEmpty();
    }


    @Test
    public void testSubmissionStatus() {
        final Submission sub = createValidSubmission();
        sub.setStatus(null);
        final Set<ConstraintViolation<Submission>> violations = validator.validate(sub);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("SUBMISSION_STATUS_REQUIRED"));
    }

    @Test
    public void testSubmissionWithDuplicateRiverInActivitiesFails() {
        final Submission sub = createValidSubmission();
        final Activity activity1 = ActivityTests.createValidActivity(sub, riverRepository.getOne(1L), 5);
        final Activity activity2 = ActivityTests.createValidActivity(sub, riverRepository.getOne(1L), 5);
        sub.setActivities(Arrays.asList(activity1, activity2));
        final Set<ConstraintViolation<Submission>> violations = validator.validate(sub);
        Assertions.assertThat(violations).hasSize(2)
                .haveAtLeast(2, SubmissionTestUtils.violationMessageMatching("ACTIVITY_RIVER_DUPLICATE_FOUND"));
    }

    @Test
    public void testSubmissionWithDuplicateSmallCatchFails() {
        final Submission sub = createValidSubmission();
        final Activity activity = ActivityTests.createValidActivity(sub, riverRepository.getOne(1L), 5);
        final List<SmallCatchCount> counts = Collections.singletonList(
                SmallCatchCountTests.createValidSmallCatchCount(methodRepository.getOne(1L), 1)
        );

        final SmallCatch smallCatch1 = SmallCatchTests.createSmallCatch(sub, activity, counts, 0);
        final SmallCatch smallCatch2 = SmallCatchTests.createSmallCatch(sub, activity, counts, 0);
        sub.setActivities(Collections.singletonList(activity));

        sub.setSmallCatches(Arrays.asList(smallCatch1, smallCatch2));
        final Set<ConstraintViolation<Submission>> violations = validator.validate(sub);
        Assertions.assertThat(violations).hasSize(2).haveAtLeast(2, SubmissionTestUtils.violationMessageMatching("SMALL_CATCH_DUPLICATE_FOUND"));
    }

    @Test
    public void testSubmissionYearTwoYearsPriorFails() {
        final Submission sub = createValidSubmission();
        sub.setSeason(getYear(-2));
        final Set<ConstraintViolation<Submission>> violations = validator.validate(sub);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("SUBMISSION_YEAR_INVALID"));
    }

    @Test
    public void testSubmissionYearInFutureFails() {
        final Submission sub = createValidSubmission();
        sub.setSeason(getYear(1));
        final Set<ConstraintViolation<Submission>> violations = validator.validate(sub);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("SUBMISSION_YEAR_INVALID"));
    }
}
