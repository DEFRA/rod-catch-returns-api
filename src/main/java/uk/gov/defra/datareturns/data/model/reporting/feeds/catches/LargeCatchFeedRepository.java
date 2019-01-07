package uk.gov.defra.datareturns.data.model.reporting.feeds.catches;

import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;


/**
 * Spring repository for {@link LargeCatchFeed} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface LargeCatchFeedRepository extends ReadOnlyRepository<LargeCatchFeed, Long> {
}
