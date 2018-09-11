package uk.gov.defra.datareturns.data.model.activities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import uk.gov.defra.datareturns.data.model.AbstractBaseEntity;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.validation.activities.ValidActivity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Set;

/**
 * RCR Catch
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcr_activity")
@GenericGenerator(name = AbstractBaseEntity.DEFINITIONS_ID_GENERATOR,
                  strategy = AbstractBaseEntity.DEFINITIONS_ID_SEQUENCE_STRATEGY,
                  parameters = {
                          @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "rcr_activity_id_seq")
                  }
)
@Audited
@Getter
@Setter
@ValidActivity
public class Activity extends AbstractBaseEntity {
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
     * The number of days fished
     */
    @Column(nullable = false)
    private Short days;

    /**
     * The significant catches recorded by the angler that are associated with this activity (fish species, rivers, mass, etc)
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "activity")
    private Set<Catch> catches;

    /**
     * Small catches - summarised counts of catches by method, month and activity
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "activity")
    private Set<SmallCatch> smallCatches;
}
