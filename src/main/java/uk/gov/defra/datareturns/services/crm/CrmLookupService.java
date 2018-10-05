package uk.gov.defra.datareturns.services.crm;

import uk.gov.defra.datareturns.data.model.licences.Activity;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

/**
 * Service to retrieve contact details from the CRM
 *
 * @author Sam Gardner-Dell
 */
public interface CrmLookupService {

    /**
     * Retrieve the Licence and contact details using the last 6 digits of the licence number
     * @param licenceNumber The last 6 digits of the licence number
     * @return Licence Returns a licence entity object
     */
    Licence getLicenceFromLicenceNumber(String licenceNumber);

    /**
     * Create an activity for a given contact and season and set status to started
     * @param contactId The CRM contact id
     * @param season The season (year) of the return
     * @return An activity entity object
     */
    Activity createActivity(String contactId, short season);


    /**
     * Update an activity for a given contact and season and set status to submitted
     * @param contactId  The CRM contact id
     * @param season The season (year) of the return
     * @return An activity entity object
     */
    Activity updateActivity(String contactId, short season);

    /**
     * Get the roles associated with an AD user in the CRM
     * @param username An AAD username
     * @param password An AAD password
     * @return An AAD identity
     */
    Identity getAuthenticatedUserRoles(String username, String password);
}
