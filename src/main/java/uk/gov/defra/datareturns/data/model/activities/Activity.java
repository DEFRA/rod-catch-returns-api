package uk.gov.defra.datareturns.data.model.activities;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.submissions.HasSubmission;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.validation.activities.ValidActivity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

/**
 * An {@link Activity} is used to record how long and angler has spent fishing on a given river.
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_activity")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uniq_activity_river_per_submission", columnNames = {"submission_id", "river_id"})
})
@Audited
@Getter
@Setter
@ValidActivity
public class Activity extends AbstractBaseEntity<Long> implements HasSubmission {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_activity_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @ApiModelProperty(readOnly = true)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * The parent submission
     */
    @ManyToOne(optional = false)
    private Submission submission;

    /**
     * The river that was fished
     */
    @ManyToOne(optional = false)
    private River river;

    /**
     * The number of days fished during the mandatory release period
     */
    @Column(nullable = false)
    private Short daysFishedWithMandatoryRelease;

    /**
     * The number of days fished at other times during the season
     */
    @Column(nullable = false)
    private Short daysFishedOther;

    /**
     * The significant catches recorded by the angler that are associated with this activity (fish species, rivers, mass, etc)
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "activity")
    @Setter(AccessLevel.NONE)
    private List<Catch> catches;

    /**
     * Small catches - summarised counts of catches by method, month and activity
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "activity")
    @Setter(AccessLevel.NONE)
    private List<SmallCatch> smallCatches;
}
