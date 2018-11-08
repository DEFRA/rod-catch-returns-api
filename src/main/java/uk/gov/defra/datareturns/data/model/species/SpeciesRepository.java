package uk.gov.defra.datareturns.data.model.species;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.model.ReferenceDataRepository;


/**
 * Spring REST repository for {@link Species} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface SpeciesRepository extends ReferenceDataRepository<Species, Long> {
}
