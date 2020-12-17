package uk.gov.defra.datareturns.test.activities;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionSource;
import uk.gov.defra.datareturns.test.submissions.SubmissionTests;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.violationMessageMatching;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@Slf4j
public class ActivityTests {
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private Validator validator;

    public static Activity createValidActivity(final Submission submission, final River river, final int daysFishedWithMandatoryRelease,
                                               final int daysFishedOther) {
        final Activity activity = new Activity();
        activity.setRiver(river);
        activity.setDaysFishedWithMandatoryRelease((short) daysFishedWithMandatoryRelease);
        activity.setDaysFishedOther((short) daysFishedOther);
        activity.setSubmission(submission);
        return activity;
    }

    @Test
    @WithAdminUser
    public void testValidActivityAdminUser() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 100, -100);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    @WithMockUser
    public void testValidActivityWithWebUser() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 100, 100);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    @WithAdminUser
    public void testActivityWithoutSubmissionFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 5, 5);
        activity.setSubmission(null);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("ACTIVITY_SUBMISSION_REQUIRED"));
    }

    @Test
    @WithAdminUser
    public void testActivityWithoutRiverFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 10, 10);
        activity.setRiver(null);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("ACTIVITY_RIVER_REQUIRED"));
    }

    @Test
    @WithAdminUser
    public void testActivityWithoutMandatoryReleaseDaysFishedFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 1, 1);
        activity.setDaysFishedWithMandatoryRelease(null);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_WITH_MANDATORY_RELEASE_REQUIRED"));
    }


    @Test
    @WithAdminUser
    public void testActivityWithoutOtherDaysFishedFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 1, 1);
        activity.setDaysFishedOther(null);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_OTHER_REQUIRED"));
    }

    @Test
    @WithAdminUser
    public void testActivityWithNegativeDaysFishedWithMandatoryReleaseFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), -1, 1);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_WITH_MANDATORY_RELEASE_NEGATIVE"));
    }

    @Test
    @WithAdminUser
    public void testActivityWithNegativeDaysFishedOtherFails() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 1, -1);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_OTHER_NEGATIVE"));
    }

    @Test
    @WithMockUser
    public void testActivityWithZeroDaysFailsForWebUser() {
        final Submission sub = SubmissionTests.createValidSubmission();
        sub.setSource(SubmissionSource.WEB);
        final Activity activity = createValidActivity(sub, getRandomRiver(), 0, 0);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_NOT_GREATER_THAN_ZERO"));
    }

    @Test
    @WithAdminUser
    public void testActivityWithZeroDaysSucceedsForAdminUser() {
        final Submission sub = SubmissionTests.createValidSubmission();
        sub.setSource(SubmissionSource.WEB);
        final Activity activity = createValidActivity(sub, getRandomRiver(), 0, 0);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    @WithAdminUser
    public void testActivityWithNonPositiveDaysIsValidForPaperReturns() {
        final Submission sub = SubmissionTests.createValidSubmission();
        sub.setSource(SubmissionSource.PAPER);
        final Activity activity = createValidActivity(sub, getRandomRiver(), 0, 0);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    @WithAdminUser
    public void testActivityWithMaxDaysWithMandatoryReleaseExceededNonLeapYear() {
        final Submission sub = SubmissionTests.createValidSubmission();
        sub.setSeason((short) 2018);

        final Activity activity = createValidActivity(sub, getRandomRiver(), 167, 0);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(0, violationMessageMatching("ACTIVITY_DAYS_FISHED_WITH_MANDATORY_RELEASE_MAX_EXCEEDED"));

        final Activity activity2 = createValidActivity(sub, getRandomRiver(), 168, 0);
        final Set<ConstraintViolation<Activity>> violations2 = validator.validate(activity2);
        Assertions.assertThat(violations2).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_WITH_MANDATORY_RELEASE_MAX_EXCEEDED"));
    }

    @Test
    @WithAdminUser
    public void testActivityWithMaxDaysWithMandatoryReleaseExceededForLeapYear() {
        final Submission sub = SubmissionTests.createValidSubmission();
        sub.setSeason((short) 2020);

        final Activity activity = createValidActivity(sub, getRandomRiver(), 168, 0);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(0, violationMessageMatching("ACTIVITY_DAYS_FISHED_WITH_MANDATORY_RELEASE_MAX_EXCEEDED"));

        final Activity activity2 = createValidActivity(sub, getRandomRiver(), 169, 0);
        final Set<ConstraintViolation<Activity>> violations2 = validator.validate(activity2);
        Assertions.assertThat(violations2).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_WITH_MANDATORY_RELEASE_MAX_EXCEEDED"));
    }

    @Test
    @WithAdminUser
    public void testActivityWithMaxDaysOtherExceeded() {
        final Activity activity = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 0, 198);
        final Set<ConstraintViolation<Activity>> violations = validator.validate(activity);
        Assertions.assertThat(violations).haveExactly(0, violationMessageMatching("ACTIVITY_DAYS_FISHED_OTHER_MAX_EXCEEDED"));


        final Activity activity2 = createValidActivity(SubmissionTests.createValidSubmission(), getRandomRiver(), 0, 199);
        final Set<ConstraintViolation<Activity>> violations2 = validator.validate(activity2);
        Assertions.assertThat(violations2).haveExactly(1, violationMessageMatching("ACTIVITY_DAYS_FISHED_OTHER_MAX_EXCEEDED"));
    }

    private River getRandomRiver() {
        return riverRepository.getOne(RandomUtils.nextLong(1, 100));
    }
}
