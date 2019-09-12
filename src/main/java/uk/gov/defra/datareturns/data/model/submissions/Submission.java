package uk.gov.defra.datareturns.data.model.submissions;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.activities.Activity;
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
 * An angler's catch return for a given season.
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
    @Setter(AccessLevel.NONE)
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
     * Is this entry excluded from reporting
     */
    @Column
    private boolean reportingExclude = false;
}
