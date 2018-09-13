package uk.gov.defra.datareturns.data.model;

import uk.gov.defra.datareturns.data.model.submissions.Submission;

public interface HasSubmission {

    Submission getSubmission();

    void setSubmission(Submission submission);
}
