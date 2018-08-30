package uk.gov.defra.datareturns.data.model.rivers;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.BaseRepository;


/**
 * Spring REST repository for {@link River} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface RiverRepository extends BaseRepository<River, Long> {
}
