package uk.gov.defra.datareturns.data.model.method;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractRestrictedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * RCR Catch method (e.g. Fly, Spinner, Bait) reference data
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_method")
@Audited
@Getter
@Setter
public class Method extends AbstractRestrictedEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_method_id_seq";

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
     * The method name
     */
    @Column
    private String name;
}
