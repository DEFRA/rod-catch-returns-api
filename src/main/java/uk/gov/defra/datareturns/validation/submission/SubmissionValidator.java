package uk.gov.defra.datareturns.validation.submission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.validation.AbstractConstraintValidator;

import javax.validation.ConstraintValidatorContext;
import java.util.Calendar;

/**
 * Validate a {@link Submission} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class SubmissionValidator extends AbstractConstraintValidator<ValidSubmission, Submission> {

    private static final String PROPERTY_CONTACT_ID = "contactId";
    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_SEASON = "season";


    @Override
    public void initialize(final ValidSubmission constraintAnnotation) {
        super.addChecks(this::checkContact, this::checkSubmissionStatus, this::checkSubmissionSeason);
    }

    /**
     * Check that a contact id is provided and exists in the CRM
     *
     * @param submission the {@link Submission} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkContact(final Submission submission, final ConstraintValidatorContext context) {
        return StringUtils.isNotBlank(submission.getContactId()) || handleError(context, "CONTACT_ID_REQUIRED", PROPERTY_CONTACT_ID);
    }

    /**
     * Check that the submission status is provided
     *
     * @param submission the {@link Submission} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkSubmissionStatus(final Submission submission, final ConstraintValidatorContext context) {
        return submission.getStatus() != null || handleError(context, "STATUS_REQUIRED", PROPERTY_STATUS);
    }

    /**
     * Check that the season is provided and not in the future
     *
     * @param submission the {@link Submission} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkSubmissionSeason(final Submission submission, final ConstraintValidatorContext context) {
        final int currentSeason = Calendar.getInstance().get(Calendar.YEAR);
        return (submission.getSeason() != null && submission.getSeason() <= currentSeason) || handleError(context, "SEASON_INVALID", PROPERTY_SEASON);
    }

    @Override
    public String getErrorPrefix() {
        return "SUBMISSION";
    }
}
