package uk.gov.defra.datareturns.data.model.rivers;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractRestrictedEntity;
import uk.gov.defra.datareturns.data.model.catchments.Catchment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

/**
 * RCR River reference data
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_river")
@Audited
@Getter
@Setter
public class River extends AbstractRestrictedEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_river_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @ApiModelProperty(readOnly = true)
    @Setter(AccessLevel.NONE)
    private Long id;

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
