package uk.gov.defra.datareturns.data.model.reporting.feeds.catches;

import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;


/**
 * Spring repository for {@link SmallCatchFeed} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface SmallCatchFeedRepository extends ReadOnlyRepository<SmallCatchFeed, String> {
}
