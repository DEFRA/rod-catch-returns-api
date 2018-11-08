package uk.gov.defra.datareturns.services.crm.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class Identity implements CrmBaseEntity {
    private final Set<String> roles = new HashSet<>();
}
