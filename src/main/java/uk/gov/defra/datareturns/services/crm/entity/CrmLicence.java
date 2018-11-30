package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CrmLicence implements CrmCall<Licence> {
    /**
     * the id associated with the contact
     */
    @JsonProperty("ContactId")
    @NotNull
    @Length(min = 1)
    private String id;

    @JsonProperty("Postcode")
    @NotNull
    @Length(min = 1)
    private String postcode;

    @JsonProperty("ReturnPermissionNumber")
    @NotNull
    @Length(min = 1)
    private String permissionNumber;

    @Override
    @JsonIgnore
    public Licence getBaseEntity() {
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

        public String getQueryName() {
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
