package uk.gov.defra.datareturns.data.model.reporting.referencedata.locations;

import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;

/**
 * Spring repository for {@link LocationEntry} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface LocationEntryRepository extends ReadOnlyRepository<LocationEntry, Long> {
}
