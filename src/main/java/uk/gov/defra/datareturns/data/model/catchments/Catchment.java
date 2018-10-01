package uk.gov.defra.datareturns.data.model.catchments;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.regions.Region;
import uk.gov.defra.datareturns.data.model.rivers.River;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import java.util.List;

/**
 * RCR Catchments
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_catchment")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_catchment_id_seq")
                  }
)
@Audited
@Getter
@Setter
public class Catchment extends AbstractBaseEntity {
    /**
     * The catchment name
     */
    private String name;

    /**
     * The {@link Region} associated with this {@link Catchment}
     */
    @ManyToOne(optional = false)
    private Region region;

    /**
     * The rivers associated with this {@link Catchment}
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "catchment")
    @Valid
    private List<River> rivers;
}
