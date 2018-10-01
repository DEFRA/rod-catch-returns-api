package uk.gov.defra.datareturns.data.model.reporting.catches.summary;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.defra.datareturns.data.ReadOnlyRepository;

import java.util.List;


/**
 * Spring repository for {@link CatchSummary} entities
 *
 * @author Sam Gardner-Dell
 */
@Repository
public interface CatchSummaryRepository extends ReadOnlyRepository<CatchSummary, Long> {

    /**
     * @param season the season that for which to fetch summary data
     * @return catch summary
     */
    List<CatchSummary> findBySeason(@Param("season") Short season);
}
