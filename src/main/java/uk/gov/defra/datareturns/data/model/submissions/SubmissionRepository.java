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
     * @param contactId the reporting reference of the submission to lookup
     * @param season    the year that the submission relates to
     * @return the {@link Submission} for the given reporting reference and year or null if not found.
     */

    @SuppressWarnings("unused")
    List<Submission> getByContactIdAndSeason(@Param("contact_id") String contactId,
                                             @Param("season") Short season);

    /**
     * Retrieve a list of {@link Submission}s for the given reporting reference
     *
     * @param contactId the reporting reference of the submission to lookup
     * @return a {@link List} of the available {@link Submission}s for the given reporting reference
     */

    @SuppressWarnings("unused")
    List<Submission> findByContactId(@Param("contact_id") String contactId);

    /**
     * Retrieve a list of {@link Submission}s for a set of contact ids for a given year
     *
     * @param contactIds the contact ids of the submissions to lookup
     * @param season     the year that the submission relates to
     * @return list of {@link Submission}s for a set of contact ids for a given year
     */
    @SuppressWarnings("unused")
    List<Submission> findByContactIdInAndSeason(@Param("contact_ids") Set<String> contactIds,
                                                @Param("season") Short season);

    @SuppressWarnings("unused")
    List<Submission> findBySeason(@Param("season") Short season);
}
