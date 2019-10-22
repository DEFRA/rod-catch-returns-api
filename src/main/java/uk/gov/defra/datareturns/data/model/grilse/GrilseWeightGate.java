package uk.gov.defra.datareturns.data.model.grilse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

@Entity(name = "rcr_grilse_weight_gate")
@Audited
@Getter
@Setter
public class GrilseWeightGate extends AbstractBaseEntity<Long> {
    public static final String SEQUENCE = "rcr_grilse_weight_gate_id_seq";

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
     * The gate name
     */
    private String name;

    /**
     * The set of catchments which this gate represents
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "gate")
    @JsonIgnoreProperties("gate")
    private List<Catchment> catchments;
}
