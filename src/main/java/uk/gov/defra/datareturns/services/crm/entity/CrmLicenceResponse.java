package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CrmLicenceResponse {
    @JsonAlias("defra_name")
    private String permissionNumber;

    @JsonAlias("_defra_contactid_value")
    private String contactId;
}
