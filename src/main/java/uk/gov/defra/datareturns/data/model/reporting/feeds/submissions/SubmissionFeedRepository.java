package uk.gov.defra.datareturns.data.model.reporting.feeds.submissions;

import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;


/**
 * Spring repository for {@link SubmissionFeed} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface SubmissionFeedRepository extends ReadOnlyRepository<SubmissionFeed, String> {
}
