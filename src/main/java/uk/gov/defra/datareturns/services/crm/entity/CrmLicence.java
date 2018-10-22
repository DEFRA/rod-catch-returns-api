package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
        if (returnStatus.equals("error")) {
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
     * This Query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class LicenceQuery implements CRMQuery<CrmLicence> {
        private Query query;

        public Class<CrmLicence> getEntityClass() {
            return CrmLicence.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_GetContactByLicenseNumber";
        }

        @Getter
        @Setter
        @ToString
        public static class Query implements CRMQuery.Query {
            @JsonProperty("PermissionNumber")
            private String permissionNumber;
        }
    }
}
