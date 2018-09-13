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
        super.addChecks(this::checkContact, this::checkSubmissionStatus, this::checkSubmissionYear);
    }

    private boolean checkContact(final Submission submission, final ConstraintValidatorContext context) {
        // FIXME: Lookup contact id in CRM.
//        return handleError(context, "REPORTING_REFERENCE_NOT_CONFIGURED_FOR_PI", b -> b.addPropertyNode("contactId"));
        return true;
    }

    private boolean checkSubmissionStatus(final Submission submission, final ConstraintValidatorContext context) {
        return submission.getStatus() != null || handleError(context, "STATUS_REQUIRED", b -> b.addPropertyNode("status"));
    }

    private boolean checkSubmissionYear(final Submission submission, final ConstraintValidatorContext context) {
        final int currentSubmissionYear = Calendar.getInstance().get(Calendar.YEAR);
        final int oldestAllowed = currentSubmissionYear - 1;
        if (submission.getSeason() == null
                || submission.getSeason() > currentSubmissionYear
                || submission.getSeason() < oldestAllowed) {
            return handleError(context, "YEAR_INVALID", b -> b.addPropertyNode("season"));
        }
        return true;
    }

    @Override
    public String getErrorPrefix() {
        return "SUBMISSION";
    }
}
