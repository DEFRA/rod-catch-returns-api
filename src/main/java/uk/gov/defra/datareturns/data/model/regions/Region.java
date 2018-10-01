package uk.gov.defra.datareturns.data.model.regions;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.catchments.Catchment;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import java.util.List;

/**
 * RCR Region
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_region")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_region_id_seq")
                  }
)
@Audited
@Getter
@Setter
public class Region extends AbstractBaseEntity {
    /**
     * The region name
     */
    private String name;

    /**
     * The catchments associated with this Region
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "region")
    @Valid
    private List<Catchment> catchments;
}
