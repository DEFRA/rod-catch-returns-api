package uk.gov.defra.datareturns.data.model.submissions;

/**
 * Interface for entities with a relationship to the {@link Submission}
 *
 * @author Sam Gardner-Dell
 */
public interface HasSubmission {

    /**
     * @return the associated {@link Submission}
     */
    Submission getSubmission();

    /**
     * Set the associated {@link Submission}
     *
     * @param submission the {@link Submission} to be associated
     */
    void setSubmission(Submission submission);
}
