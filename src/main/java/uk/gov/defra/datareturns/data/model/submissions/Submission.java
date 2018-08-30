package uk.gov.defra.datareturns.data.model.submissions;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.springframework.data.rest.core.annotation.RestResource;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.validation.submission.ValidSubmission;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import java.util.Set;

/**
 * RCR Submission
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_submission")
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uniq_contact_id_and_year", columnNames = {"contactId", "seasonEnding"})
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
    @Column(nullable = false)
    private Short seasonEnding;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "submission")
    @RestResource(path = "activities", rel = "activities")
    @JsonProperty(value = "activities")
    @JsonManagedReference
    @Valid
    private Set<Activity> submissionActivities;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "submission")
    @RestResource(path = "catches", rel = "catches")
    @JsonProperty(value = "catches")
    @JsonManagedReference
    @Valid
    private Set<Catch> submissionCatches;

}
