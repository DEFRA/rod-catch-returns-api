package uk.gov.defra.datareturns.data.model.catches;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.HasSubmission;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.species.Species;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.validation.catches.ValidCatch;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import java.util.Date;

/**
 * RCR Catch
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_catch")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_catch_id_seq")
                  }
)
@Audited
@Getter
@Setter
@ValidCatch
public class Catch extends AbstractBaseEntity implements HasSubmission {
    /**
     * The parent submission
     */
    @ManyToOne(optional = false)
    private Submission submission;

    /**
     * The activity associated with this catch
     */
    @ManyToOne(optional = false)
    private Activity activity;

    /**
     * The date of the catch
     */
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @SuppressFBWarnings("EI_EXPOSE_REP")
    private Date dateCaught;

    /**
     * The species of catch (Salmon, Sea Trout)
     */
    @ManyToOne(optional = false)
    private Species species;

    /**
     * The mass of the catch
     */
    @Embedded
    @Valid
    private CatchMass mass = new CatchMass();

    /**
     * The method used (Fly, Spinner, Bait)
     */
    @ManyToOne(optional = false)
    private Method method;

    /**
     * Was the catch released?
     */
    @Column
    private boolean released;

    /**
     * Is this entry excluded from reporting
     */
    @Column
    private boolean reportingExclude = false;
}
