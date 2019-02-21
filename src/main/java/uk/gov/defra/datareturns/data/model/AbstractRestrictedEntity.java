package uk.gov.defra.datareturns.data.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractRestrictedEntity<I> extends AbstractBaseEntity<I> {
    @Column
    private boolean internal = false;
}
