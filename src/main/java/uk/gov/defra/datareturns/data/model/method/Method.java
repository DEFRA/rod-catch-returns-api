package uk.gov.defra.datareturns.data.model.method;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;

import javax.persistence.Entity;

/**
 * RCR Catch method (e.g. Fly, Spinner, Bait) reference data
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_method")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_method_id_seq")
                  }
)
@Audited
@Getter
@Setter
public class Method extends AbstractBaseEntity {
    /**
     * The method name
     */
    private String name;
}
