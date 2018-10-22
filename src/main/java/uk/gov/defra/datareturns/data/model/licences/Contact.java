package uk.gov.defra.datareturns.data.model.licences;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.defra.datareturns.services.crm.entity.CrmBaseEntity;

/**
 * Represents a contact entry within the CRM
 *
 * @author Sam Gardner-Dell
 */
@Getter
@Setter
@ToString
public class Contact implements CrmBaseEntity {
    /**
     * the id associated with the contact
     */
    private String id;

    /**
     * the postcode associated with the contact
     */
    private String postcode;
}
