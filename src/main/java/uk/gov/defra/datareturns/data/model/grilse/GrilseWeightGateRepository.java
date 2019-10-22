package uk.gov.defra.datareturns.data.model.grilse;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.BaseRepository;

@RepositoryRestResource
public interface GrilseWeightGateRepository extends BaseRepository<GrilseWeightGate, Long> {

}
