package uk.gov.defra.datareturns.data.model.rivers;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.AbstractSecuredEntity;
import uk.gov.defra.datareturns.data.model.catchments.Catchment;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

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
public class River extends AbstractSecuredEntity {
    /**
     * The river name
     */
    private String name;

    /**
     * The {@link Catchment} associated with this {@link River}
     */
    @ManyToOne(optional = false)
    private Catchment catchment;
}
