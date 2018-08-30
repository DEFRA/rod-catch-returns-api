package uk.gov.defra.datareturns.data.model.submissions;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.defra.datareturns.data.BaseRepository;

import java.util.List;
import java.util.Set;


/**
 * Spring REST repository for {@link Submission} entities
 *
 * @author Sam Gardner-Dell
 */
@RepositoryRestResource
public interface SubmissionRepository extends BaseRepository<Submission, Long> {

    /**
     * Retrieve a submission by a given reporting reference and the year that it is applicable to
     *
     * @param contactId    the reporting reference of the submission to lookup
     * @param seasonEnding the year that the submission relates to
     * @return the {@link Submission} for the given reporting reference and year or null if not found.
     */

    @SuppressWarnings("unused")
    List<Submission> getByContactIdAndSeasonEnding(@Param("contact_id") Long contactId,
                                                   @Param("season_ending") Short seasonEnding);

    /**
     * Retrieve a list of {@link Submission}s for the given reporting reference
     *
     * @param contactId the reporting reference of the submission to lookup
     * @return a {@link List} of the available {@link Submission}s for the given reporting reference
     */

    @SuppressWarnings("unused")
    List<Submission> findByContactId(@Param("contact_id") Long contactId);

    /**
     * Retrieve a list of {@link Submission}s for a set of reporting references for a given year
     *
     * @return
     */
    @SuppressWarnings("unused")
    List<Submission> findByContactIdInAndSeasonEnding(@Param("contact_ids") Set<Long> contactIds,
                                                        @Param("season_ending") Short seasonEnding);

    @SuppressWarnings("unused")
    List<Submission> findBySeasonEnding(@Param("season_ending") Short seasonEnding);
}
