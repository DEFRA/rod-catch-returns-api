package uk.gov.defra.datareturns.data.model.smallcatches;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.BaseRepository;


/**
 * Spring REST repository for {@link SmallCatch} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface SmallCatchRepository extends BaseRepository<SmallCatch, Long> {
}
