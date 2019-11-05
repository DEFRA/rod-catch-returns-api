package uk.gov.defra.datareturns.data.model.grilse;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.BaseRepository;

import java.util.List;


/**
 * Spring REST repository for {@link GrilseProbability} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface GrilseProbabilityRepository extends BaseRepository<GrilseProbability, Long> {
    List<GrilseProbability> findBySeasonAndGate(short season, GrilseWeightGate gate);
}
