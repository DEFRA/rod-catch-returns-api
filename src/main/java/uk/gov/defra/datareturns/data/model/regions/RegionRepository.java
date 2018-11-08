package uk.gov.defra.datareturns.data.model.regions;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.model.ReferenceDataRepository;


/**
 * Spring REST repository for {@link Region} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface RegionRepository extends ReferenceDataRepository<Region, Long> {
}
