package uk.gov.defra.datareturns.data.model.grilse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.univocity.parsers.annotations.Headers;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Headers
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class GrilseWeightGate extends AbstractBaseEntity<Short> {

    public static final String SEQUENCE = "rcr_grilse_weight_gate_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @ApiModelProperty(accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Setter(AccessLevel.NONE)
    private Short id;

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
