package uk.gov.defra.datareturns.services.crm;

import org.springframework.lang.NonNull;
import uk.gov.defra.datareturns.data.model.licences.Licence;

import java.util.List;
import java.util.Optional;

/**
 * Service to retrieve contact details from the CRM
 *
 * @author Graham Willis
 */
public interface CrmLookupService {

    /**
     * Retrieve the Licence and contact details using the last 6 digits of the licence number
     *
     * @param licenceNumber The last 6 digits of the licence number
     * @param postcode      the postcode to cross-check against the licence number
     * @return Licence Returns a licence entity object
     */
    Optional<Licence> getLicence(final String licenceNumber, final String postcode);

    /**
     * Retrieve the Licence and contact details using the full licence number
     *
     * @param fullLicenceNumber The full licence number
     * @return Licence Returns a licence entity object
     */
    Optional<Licence> getLicence(final String fullLicenceNumber);

    /**
     * Create an activity for a given contact and season and set status to started
     *
     * @param contactId The CRM contact id
     * @param season    The season (year) of the return
     */
    void createActivity(String contactId, short season);


    /**
     * Update an activity for a given contact and season and set status to submitted
     *
     * @param contactId The CRM contact id
     * @param season    The season (year) of the return
     */
    void updateActivity(String contactId, short season);

    /**
     * Get the roles associated with an AD user in the CRM
     *
     * @param username An AAD username
     * @param password An AAD password
     * @return List<String> the user's roles (if any)
     */
    @NonNull
    List<String> getAuthenticatedUserRoles(String username, String password);
}
