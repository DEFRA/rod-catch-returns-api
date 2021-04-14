package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrmLicenceResponse {
    @JsonAlias("defra_name")
    private String permissionNumber;

    @JsonAlias("defra_ContactId")
    private CrmContactResponse contact;
}
