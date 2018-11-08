package uk.gov.defra.datareturns.data.model.submissions;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.validation.submission.ValidSubmission;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import java.util.List;

/**
 * RCR Submission
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_submission")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uniq_contact_id_and_season", columnNames = {"contactId", "season"})
})
@Audited
@Getter
@Setter
@ValidSubmission
public class Submission extends AbstractBaseEntity<Long> {
    /**
     * Database sequence name for this entity
     */
    public static final String SEQUENCE = "rcr_submission_id_seq";

    /**
     * Primary key
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = SEQUENCE, sequenceName = SEQUENCE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE)
    @ApiModelProperty(readOnly = true)
    private Long id;

    /**
     * The contact identifier
     */
    @Column(nullable = false)
    private String contactId;

    /**
     * The season (year) pertaining to the submission
     */
    @Column(nullable = false, updatable = false)
    private Short season;

    /**
     * The submission status
     */
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    /**
     * The submission source
     */
    @Enumerated(EnumType.STRING)
    private SubmissionSource source;

    /**
     * The activities recorded by the angler for the season (the time in days spent on each river)
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "submission")
    @Valid
    private List<Activity> activities;

    /**
     * The significant catches recorded by the angler for the season (fish species, rivers, mass, etc)
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "submission")
    @Valid
    private List<Catch> catches;

    /**
     * Small catches - summarised counts of catches by method, month and river
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "submission")
    @Valid
    private List<SmallCatch> smallCatches;

    /**
     * Is this entry excluded from reporting
     */
    @Column
    private boolean reportingExclude = false;
}
