package uk.gov.defra.datareturns.validation.submission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.submissions.Submission;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Calendar;

import static uk.gov.defra.datareturns.validation.util.ValidationUtil.handleError;

/**
 * Validate a {@link Submission} object
 *
 * @author Sam Gardner-Dell
 */
@RequiredArgsConstructor
@Slf4j
public class SubmissionValidator implements ConstraintValidator<ValidSubmission, Submission> {
    @Override
    public void initialize(final ValidSubmission constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Submission submission, final ConstraintValidatorContext context) {
        boolean valid = checkContact(submission, context);
        valid = checkSubmissionYear(submission, context) && valid;
        return valid;
    }

    private boolean checkContact(final Submission submission, final ConstraintValidatorContext context) {
        // FIXME: Lookup contact id in CRM.
//        return handleError(context, "SUBMISSION_REPORTING_REFERENCE_NOT_CONFIGURED_FOR_PI", b -> b.addPropertyNode("contactId"));
        return true;
    }


    private boolean checkSubmissionYear(final Submission submission, final ConstraintValidatorContext context) {
        final int currentSubmissionYear = Calendar.getInstance().get(Calendar.YEAR);
        final int oldestAllowed = currentSubmissionYear - 1;
        if (submission.getSeasonEnding() == null
                || submission.getSeasonEnding() > currentSubmissionYear
                || submission.getSeasonEnding() < oldestAllowed) {
            return handleError(context, "SUBMISSION_YEAR_INVALID", b -> b.addPropertyNode("seasonEnding"));
        }
        return true;
    }
}
