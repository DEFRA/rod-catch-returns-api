package uk.gov.defra.datareturns.data.model.submissions;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
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
import javax.persistence.OneToMany;
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
        @UniqueConstraint(name = "uniq_contact_id_and_year", columnNames = {"contactId", "season"})
})
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_submission_id_seq")
                  }
)
@Audited
@Getter
@Setter
@ValidSubmission
public class Submission extends AbstractBaseEntity {
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
}
