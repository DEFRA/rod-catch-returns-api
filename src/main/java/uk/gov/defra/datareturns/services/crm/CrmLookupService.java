package uk.gov.defra.datareturns.services.crm;

import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;

/**
 * Service to retrieve contact details from the CRM
 *
 * @author Sam Gardner-Dell
 */
public interface CrmLookupService {

    /**
     * Retrieve a licence for the given lookup string (usually the last 6 digits of the licence number)
     *
     * @param lookup the lookup string (usually the last 6 digits of the licence number)
     * @return the {@link Licence} object for the given lookup string or null if not found
     */
    Licence getLicence(String lookup);

    /**
     * Retrieve a contact for the given contact id
     *
     * @param contactId the contact id used to retrieve the {@link Contact} object
     * @return the {@link Contact} object for the given contact id or null if not found
     */
    Contact getContact(String contactId);
}
