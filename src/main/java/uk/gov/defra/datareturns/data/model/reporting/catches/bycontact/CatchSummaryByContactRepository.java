package uk.gov.defra.datareturns.data.model.reporting.catches.bycontact;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;

import java.util.List;


/**
 * Spring repository for {@link CatchSummaryByContact} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface CatchSummaryByContactRepository extends ReadOnlyRepository<CatchSummaryByContact, Long> {

    /**
     * @param season the season that for which to fetch summary data
     * @return catch summary
     */
    List<CatchSummaryByContact> findBySeason(@Param("season") Short season);
}
