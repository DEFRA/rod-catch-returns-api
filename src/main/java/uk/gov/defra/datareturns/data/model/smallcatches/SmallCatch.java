package uk.gov.defra.datareturns.data.model.smallcatches;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.validation.smallcatches.ValidSmallCatch;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.Month;
import java.util.Set;

/**
 * RCR Small Catch
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_small_catch")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_small_catch_id_seq")
                  }
)
@Audited
@Getter
@Setter
@ValidSmallCatch
public class SmallCatch extends AbstractBaseEntity {
    /**
     * The parent submission
     */
    @ManyToOne(optional = false)
    private Submission submission;

    /**
     * The river where the catch was made
     */
    @ManyToOne(optional = false)
    private River river;

    /**
     * The month this record relates to
     */
    @Enumerated(EnumType.STRING)
    private Month month;

    /**
     * Small catches counts
     */
    @ElementCollection
    @CollectionTable(name = "rcr_small_catch_counts", joinColumns = @JoinColumn(name = "small_catch_id"))
    private Set<SmallCatchCount> counts;

    /**
     * The number released
     */
    private int released;
}
