package uk.gov.defra.datareturns.data.model.catchments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.grilse.GrilseWeightGate;
import uk.gov.defra.datareturns.data.model.regions.Region;
import uk.gov.defra.datareturns.data.model.rivers.River;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import java.util.List;

/**
 * RCR Catchments
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_catchment")
@Audited
@Getter
@Setter
public class Catchment extends AbstractBaseEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_catchment_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @ApiModelProperty(accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Setter(AccessLevel.NONE)
    private Long id;

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
    @JsonIgnore
    private List<River> rivers;

    /**
     *
     */
    @ManyToOne
    private GrilseWeightGate gate;

}
