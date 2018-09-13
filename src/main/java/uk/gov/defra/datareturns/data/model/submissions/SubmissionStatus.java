package uk.gov.defra.datareturns.data.model.submissions;

import lombok.Getter;

@Getter
public enum SubmissionStatus {
    INCOMPLETE(true),
    SUBMITTED(false);

    private final boolean open;

    SubmissionStatus(final boolean open) {
        this.open = open;
    }
}
