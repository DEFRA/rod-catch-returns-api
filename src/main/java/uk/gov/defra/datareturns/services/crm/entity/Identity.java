package uk.gov.defra.datareturns.services.crm.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class Identity implements CrmBaseEntity {
    private Set<String> roles;
}
