package uk.gov.defra.datareturns.data.model.grilse;

import com.univocity.parsers.annotations.Parsed;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * An {@link GrilseProbability} entity is use to store probability data for a Salmon being a Grilse based on electronic measurement gate data.
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_grilse_probability")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uniq_grilse_probability_key", columnNames = {"season", "gate_id", "month", "mass_lbs"})
})
@Audited
@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class GrilseProbability extends AbstractBaseEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_grilse_probability_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * The season the probability data relates to
     */
    @Column(name = "season")
    @Parsed(field = "Season")
    private Short season;

    /**
     * The grilse weight gate that the probability relates to
     */
    @ManyToOne(targetEntity = GrilseWeightGate.class)
    private GrilseWeightGate gate;

    /**
     * The month (1-based index) this probability data relates to
     */
    @Column(name = "month")
    @Parsed(field = "Month")
    private Short month;

    /**
     * The mass associated with this probability (in imperial pounds, data matched to the nearest whole pound)
     */
    @Column(name = "mass_lbs")
    @Parsed(field = "Mass (lbs)")
    private Short massInPounds;

    /**
     * The probability entry (decimal between 0 and 1)
     */
    @Column(precision = 17, scale = 16)
    @Parsed(field = "Probability")
    private BigDecimal probability;
}
