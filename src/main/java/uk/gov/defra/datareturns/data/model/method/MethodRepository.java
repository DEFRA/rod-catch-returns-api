package uk.gov.defra.datareturns.data.model.method;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.model.RestrictedEntityFilteringRepository;


/**
 * Spring REST repository for {@link Method} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface MethodRepository extends RestrictedEntityFilteringRepository<Method, Long> {
}
