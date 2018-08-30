package uk.gov.defra.datareturns.data.model.catches;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.BaseRepository;


/**
 * Spring REST repository for {@link Catch} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface CatchRepository extends BaseRepository<Catch, Long> {
}
