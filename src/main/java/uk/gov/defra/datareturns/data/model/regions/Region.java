package uk.gov.defra.datareturns.data.model.regions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.catchments.Catchment;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import java.util.List;

/**
 * RCR Region
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_region")
@Audited
@Getter
@Setter
public class Region extends AbstractBaseEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_region_id_seq";

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
     * The region name
     */
    private String name;

    /**
     * The catchments associated with this Region
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "region")
    @JsonIgnore
    private List<Catchment> catchments;
}
