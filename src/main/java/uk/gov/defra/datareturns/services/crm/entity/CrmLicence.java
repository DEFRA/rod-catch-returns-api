package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmLicence implements CrmCall<Licence> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/y");
    /**
     * the id associated with the contact
     */
    @JsonProperty("ContactId")
    private String id;

    @JsonProperty("Postcode")
    private String postcode;

    @JsonProperty("ReturnPermissionNumber")
    private String permissionNumber;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("DateOfBirth")
    private String dob;

    @JsonProperty("Premises")
    private String premises;

    @JsonProperty("Street")
    private String street;

    @JsonProperty("Town")
    private String town;

    @JsonProperty("Locality")
    private String locality;

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
        Licence licence = new Licence();
        licence.setLicenceNumber(permissionNumber);
        Contact contact = new Contact();
        contact.setId(id);
        contact.setPostcode(postcode);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setDob(LocalDate.parse(dob, formatter));
        contact.setPremises(premises);
        contact.setStreet(street);
        contact.setTown(town);
        contact.setLocality(locality);
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
