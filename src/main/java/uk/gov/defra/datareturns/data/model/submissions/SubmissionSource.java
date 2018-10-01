package uk.gov.defra.datareturns.data.model.submissions;

/**
 * The source of a {@link Submission}
 */
public enum SubmissionSource {
    /**
     * Submission returned via gov.uk digital service
     */
    WEB,
    /**
     * Submission submitted via paper form
     */
    PAPER
}
