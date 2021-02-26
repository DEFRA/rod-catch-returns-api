package uk.gov.defra.datareturns.services.crm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@Getter
@Setter
@ToString
public class CrmResponseEntity {
    private ArrayList<CrmLicenceResponse> value;
}
