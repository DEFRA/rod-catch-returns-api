package uk.gov.defra.datareturns.data.model.reporting.feeds.activities;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Reporting view for activity information
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_feed_activities")
@Getter
@Headers
public class ActivityFeed {
    @Id
    @Parsed(field = "ID")
    private Long id;
    /**
     * Season (for filtering only), not part of report feed
     */
    @Basic
    private Short season;
    @Basic
    @Parsed(field = "Submission ID")
    private Long submissionId;
    @Basic
    @Parsed(field = "River ID")
    private Long riverId;
    @Basic
    @Parsed(field = "Days Fished (Mandatory Release)")
    private Short daysFishedWithMandatoryRelease;
    @Basic
    @Parsed(field = "Days Fished (Other)")
    private Short daysFishedOther;
}
