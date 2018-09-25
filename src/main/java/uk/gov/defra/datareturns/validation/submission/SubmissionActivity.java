package uk.gov.defra.datareturns.validation.submission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;

/**
 * Used to intecept POST and PUT submission events (the status change)
 * and to push the corresponding activates into the CRM
 */
@Component
@RepositoryEventHandler(Submission.class)
@RequiredArgsConstructor
public class SubmissionActivity {
    private final CrmLookupService lookupService;

    @HandleAfterCreate
    public void createSubmission(Submission submission) {
        lookupService.createActivity(submission.getContactId(), submission.getSeason());
    }

    @HandleAfterSave
    public void saveSubmission(Submission submission) {
        lookupService.updateActivity(submission.getContactId(), submission.getSeason());
    }
}
