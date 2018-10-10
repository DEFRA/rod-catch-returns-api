package uk.gov.defra.datareturns.validation.submission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return submission.getContactId() != null || handleError(context, "CONTACT_ID_REQUIRED", b -> b.addPropertyNode("contactId"));
        // FIXME: Lookup contact id in CRM. // return handleError(context, "CONTACT_ID_INVALID", b -> b.addPropertyNode("contactId"));
    }

    /**
     * Check that the submission status is provided
     *
     * @param submission the {@link Submission} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkSubmissionStatus(final Submission submission, final ConstraintValidatorContext context) {
        return submission.getStatus() != null || handleError(context, "STATUS_REQUIRED", b -> b.addPropertyNode("status"));
    }

    /**
     * Check that the season is provided and only allow submissions for the current or previous year
     *
     * @param submission the {@link Submission} to be validated
     * @param context    the validator context
     * @return true if valid, false otherwise
     */
    private boolean checkSubmissionSeason(final Submission submission, final ConstraintValidatorContext context) {
        final int currentSeason = Calendar.getInstance().get(Calendar.YEAR);
        final int lastSeason = currentSeason - 1;
        return (submission.getSeason() != null && submission.getSeason() <= currentSeason && submission.getSeason() >= lastSeason)
                || handleError(context, "SEASON_INVALID", b -> b.addPropertyNode("season"));
    }

    @Override
    public String getErrorPrefix() {
        return "SUBMISSION";
    }
}
