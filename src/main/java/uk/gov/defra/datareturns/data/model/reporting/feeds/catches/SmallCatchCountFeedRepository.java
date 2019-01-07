package uk.gov.defra.datareturns.data.model.reporting.feeds.catches;

import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;


/**
 * Spring repository for {@link SmallCatchCountFeed} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface SmallCatchCountFeedRepository extends ReadOnlyRepository<SmallCatchCountFeed, String> {
}
