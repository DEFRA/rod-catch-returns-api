package uk.gov.defra.datareturns.data.model.species;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(readOnly = true)
    private Long id;

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
