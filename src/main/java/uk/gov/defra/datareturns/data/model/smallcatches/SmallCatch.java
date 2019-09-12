package uk.gov.defra.datareturns.data.model.smallcatches;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.gov.defra.datareturns.data.conversion.MonthConverter;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.validation.smallcatches.ValidSmallCatch;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import java.time.Month;
import java.util.List;

/**
 * Records an anglers small catches against an {@link Activity}
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_small_catch")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uniq_activity_and_month", columnNames = {"activity_id", "month"})
})
@Audited
@Getter
@Setter
@ValidSmallCatch
public class SmallCatch extends AbstractBaseEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_small_catch_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @Setter(AccessLevel.NONE)
    private Long id;

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

    /**
     * To allow FMT users to report on the default date
     */
    @Column
    private boolean noMonthRecorded = false;

}
