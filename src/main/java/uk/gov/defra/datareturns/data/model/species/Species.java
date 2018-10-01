package uk.gov.defra.datareturns.data.model.species;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * RCR Species (e.g. Salmon, Sea Trout) reference data
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_species")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_species_id_seq")
                  }
)
@Audited
@Getter
@Setter
public class Species extends AbstractBaseEntity {
    /**
     * The species name
     */
    private String name;

    /**
     * The assumed mass of a small catch entry for this species
     */
    @Column(precision = 12, scale = 6)
    private BigDecimal smallCatchMass;

}
