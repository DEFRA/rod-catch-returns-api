package uk.gov.defra.datareturns.services.crm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class CrmResponseEntity {
    private List<CrmLicenceResponse> value;
}
