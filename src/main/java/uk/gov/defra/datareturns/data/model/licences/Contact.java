package uk.gov.defra.datareturns.data.model.licences;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.defra.datareturns.services.crm.CRMEntity;

/**
 * Represents a contact entry within the CRM
 *
 * @author Sam Gardner-Dell
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact implements CRMEntity {
    /**
     * the id associated with the contact
     */
    @JsonProperty("ContactId")
    private String id;

    @JsonProperty("Postcode")
    private String postcode;

    @JsonProperty("PermissionNumber")
    private String permissionNumber;

    @JsonProperty("ReturnStatus")
    private String returnStatus;

    @JsonProperty("ErrorMessage")
    private String errorMessage;
}