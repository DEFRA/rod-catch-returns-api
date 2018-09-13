package uk.gov.defra.datareturns.test.activities;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.test.submissions.SubmissionTests;
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
public class ActivityTests {
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private Validator validator;

    public static Activity createValidActivity(final Submission submission, final River river, final int days) {
        final Activity activity = new Activity();
        activity.setRiver(river);
        activity.setDays((short) days);
        activity.setSubmission(submission);
        return activity;
    }

    @Test
    public void testValidActivity() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 100);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void testActivityWithoutSubmissionFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 100);
        activity.setSubmission(null);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("ACTIVITY_SUBMISSION_REQUIRED"));
    }

    @Test
    public void testActivityWithoutRiverFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 100);
        activity.setRiver(null);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("ACTIVITY_RIVER_REQUIRED"));
    }

    @Test
    public void testActivityWithNonPositiveDaysFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 0);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).hasSize(1)
                .haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("ACTIVITY_DAYS_NOT_GREATER_THAN_ZERO"));
    }

    @Test
    public void testActivityWithMaxDaysExceeded() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 367);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("ACTIVITY_DAYS_MAX_EXCEEDED"));
    }

    private River getRandomRiver() {
        return riverRepository.getOne(RandomUtils.nextLong(1, 100));
    }
}
