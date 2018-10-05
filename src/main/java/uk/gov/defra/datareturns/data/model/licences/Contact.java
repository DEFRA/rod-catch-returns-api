package uk.gov.defra.datareturns.data.model.licences;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.defra.datareturns.services.crm.entity.CrmBaseEntity;

import java.time.LocalDate;

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

    private String postcode;
    private String firstName;
    private String lastName;

    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate dob;

    private String premises;
    private String street;
    private String town;
    private String locality;

}
