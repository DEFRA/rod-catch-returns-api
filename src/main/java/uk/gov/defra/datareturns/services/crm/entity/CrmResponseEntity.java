package uk.gov.defra.datareturns.services.crm.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrmResponseEntity {
    private List<CrmLicenceResponse> value;
}
