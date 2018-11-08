package uk.gov.defra.datareturns.data.model.catchments;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.model.ReferenceDataRepository;


/**
 * Spring REST repository for {@link Catchment} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface CatchmentRepository extends ReferenceDataRepository<Catchment, Long> {
}
