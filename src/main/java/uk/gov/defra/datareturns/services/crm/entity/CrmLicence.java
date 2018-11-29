package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;

@Getter
@Setter
public class CrmLicence implements CrmCall<Licence> {
    /**
     * the id associated with the contact
     */
    @JsonProperty("ContactId")
    private String id;

    @JsonProperty("Postcode")
    private String postcode;

    @JsonProperty("ReturnPermissionNumber")
    private String permissionNumber;

    @JsonProperty("ReturnStatus")
    private String returnStatus;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @Override
    @JsonIgnore
    public Licence getBaseEntity() {
        if ("error".equals(returnStatus)) {
            return null;
        }
        final Licence licence = new Licence();
        licence.setLicenceNumber(permissionNumber);
        final Contact contact = new Contact();
        contact.setId(id);
        contact.setPostcode(postcode);
        licence.setContact(contact);
        return licence;
    }

    /**
     * This query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class LicenceQuery implements CRMQuery<CrmLicence> {
        private QueryParams queryParams;

        public Class<CrmLicence> getEntityClass() {
            return CrmLicence.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_GetContactByLicenceAndPostcode";
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class QueryParams {
        @JsonProperty("PermissionNumber")
        private String permissionNumber;
        @JsonProperty("InputPostCode")
        private String postcode;
    }
}
