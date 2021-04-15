package uk.gov.defra.datareturns.data.model.licences;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a contact entry within the CRM
 *
 * @author Sam Gardner-Dell
 */
@Getter
@Setter
public class Contact {
    /**
     * the id associated with the contact
     */
    private String id;

    /**
     * the postcode associated with the contact
     */
    private String postcode;

    /**
     * the full name associated with the contact
     */
    private String fullName;
}
