package uk.gov.defra.datareturns.data.model.smallcatches;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.conversion.MonthConverter;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.HasSubmission;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.validation.smallcatches.ValidSmallCatch;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import java.time.Month;
import java.util.List;

/**
 * RCR Small Catch
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_small_catch")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uniq_activity_and_month_per_submission", columnNames = {"submission_id", "activity_id", "month"})
})
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
public class SmallCatch extends AbstractBaseEntity implements HasSubmission {
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
     * The month this record relates to
     */
    @Convert(converter = MonthConverter.class)
    private Month month;

    /**
     * Small catches counts
     */
    @ElementCollection
    @CollectionTable(name = "rcr_small_catch_counts", joinColumns = @JoinColumn(name = "small_catch_id"))
    @Valid
    @NotAudited
    private List<SmallCatchCount> counts;

    /**
     * The number released
     */
    @Column
    private Short released;

    /**
     * Is this entry excluded from reporting
     */
    @Column
    private boolean reportingExclude = false;
}
