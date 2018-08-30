package uk.gov.defra.datareturns.data.model.activities;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.BaseRepository;


/**
 * Spring REST repository for {@link Activity} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface ActivityRepository extends BaseRepository<Activity, Long> {
}
