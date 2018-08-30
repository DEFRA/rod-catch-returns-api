package uk.gov.defra.datareturns.data.model.rivers;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;

import javax.persistence.Entity;

/**
 * RCR River reference data
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_river")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_river_id_seq")
                  }
)
@Audited
@Getter
@Setter
public class River extends AbstractBaseEntity {
    /**
     * The river name
     */
    private String name;
}
