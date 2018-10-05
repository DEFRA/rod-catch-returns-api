package uk.gov.defra.datareturns.data.model.licences;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.defra.datareturns.services.crm.entity.CrmBaseEntity;

/**
 * Represents a licence entry within the CRM
 *
 * @author Sam Gardner-Dell
 */
@Getter
@Setter
@ToString
public class Licence implements CrmBaseEntity {
    /**
     * The full licence number
     */
    private String licenceNumber;

    /**
     * The contact record associated with the licence
     */
    private Contact contact;
}
