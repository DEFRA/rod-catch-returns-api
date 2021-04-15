package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrmContactResponse {

    @JsonAlias("fullname")
    private String fullName;

    @JsonAlias("contactid")
    private String contactId;
}
