package uk.gov.defra.datareturns.services.crm.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Identity implements CrmBaseEntity {
    private String[] roles;
}
