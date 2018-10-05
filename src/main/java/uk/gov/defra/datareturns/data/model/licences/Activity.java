package uk.gov.defra.datareturns.data.model.licences;

import lombok.Getter;
import lombok.Setter;
import uk.gov.defra.datareturns.services.crm.entity.CrmBaseEntity;

@Getter
@Setter
public class Activity implements CrmBaseEntity {
    private String id;
}
