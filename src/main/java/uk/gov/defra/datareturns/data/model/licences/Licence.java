package uk.gov.defra.datareturns.data.model.licences;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a licence entry within the CRM
 *
 * @author Sam Gardner-Dell
 */
@Getter
@Setter
public class Licence {
    /**
     * The full licence number
     */
    private String licenceNumber;

    /**
     * The contact record associated with the licence
     */
    private Contact contact;
}
