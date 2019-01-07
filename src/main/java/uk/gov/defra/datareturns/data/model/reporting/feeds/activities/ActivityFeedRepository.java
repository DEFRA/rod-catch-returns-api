package uk.gov.defra.datareturns.data.model.reporting.feeds.activities;

import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;


/**
 * Spring repository for {@link ActivityFeed} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface ActivityFeedRepository extends ReadOnlyRepository<ActivityFeed, Long> {
}
