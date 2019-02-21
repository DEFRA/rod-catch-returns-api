package uk.gov.defra.datareturns.data.model.species;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.math.BigDecimal;

/**
 * RCR Species (e.g. Salmon, Sea Trout) reference data
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_species")
@Audited
@Getter
@Setter
@Headers
public class Species extends AbstractBaseEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_species_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @ApiModelProperty(accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Setter(AccessLevel.NONE)
    @Parsed(field = "ID")
    private Long id;

    /**
     * The species name
     */
    @Parsed(field = "Species")
    private String name;

    /**
     * The assumed mass of a small catch entry for this species
     */
    @Column(precision = 12, scale = 6)
    @Parsed(field = "Small Catch Mass")
    private BigDecimal smallCatchMass;

}
