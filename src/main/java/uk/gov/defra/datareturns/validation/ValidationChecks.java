package uk.gov.defra.datareturns.validation;

import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.submissions.Submission;

import java.util.stream.Collectors;

/**
 * Reusable validation checks for RCR
 */
public final class ValidationChecks {

    /**
     * Private constructor
     */
    private ValidationChecks() {

    }

    /**
     * Check that the given {@link River} has been defined in the activities associated with the submission
     *
     * @param submission the submission to check
     * @param river      the river to check for
     * @return true if the river is defined in the submission activities, false otherwise.
     */
    public static boolean checkRiverDefinedInActivities(final Submission submission, final River river) {
        return submission != null && submission.getActivities() != null
                && submission.getActivities().stream().map(Activity::getRiver).collect(Collectors.toSet()).contains(river);

    }
}
