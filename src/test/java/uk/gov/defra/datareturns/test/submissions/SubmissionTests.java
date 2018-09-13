package uk.gov.defra.datareturns.test.submissions;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionStatus;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;
import uk.gov.defra.datareturns.testutils.SubmissionTestUtils;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Calendar;
import java.util.Set;

/**
 * Integration tests submission-level property validation
 */
@RunWith(SpringRunner.class)
@WebIntegrationTest
@Slf4j
public class SubmissionTests {
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
