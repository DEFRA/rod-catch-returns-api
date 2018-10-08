package uk.gov.defra.datareturns.services.crm.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Identity implements CrmBaseEntity {
    private List<String> roles;
}
