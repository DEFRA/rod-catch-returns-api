package uk.gov.defra.datareturns.data.model.reporting.feeds.catches;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Reporting view for large catches
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_feed_large_catches")
@Getter
@Headers
public class LargeCatchFeed {
    @Id
    @Parsed(field = "ID")
    private Long id;
    /**
     * Season (for filtering only), not part of report feed
     */
    @Basic
    private Short season;
    @Basic
    @Parsed(field = "Activity ID")
    private Long activityId;
    @Basic
    @Temporal(TemporalType.DATE)
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @Parsed(field = "Date")
    private Date dateCaught;
    @Basic
    @Parsed(field = "Species ID")
    private Long speciesId;
    @Basic
    @Parsed(field = "Method ID")
    private Long methodId;
    @Basic
    @Parsed(field = "Mass (kg)")
    private BigDecimal massKg;
    @Basic
    @Parsed(field = "Released")
    private Boolean released;
    @Basic
    @Parsed(field = "Only Month Recorded")
    private Boolean onlyMonthRecorded;
    @Basic
    @Parsed(field = "No Date Recorded")
    private Boolean noDateRecorded;
}
