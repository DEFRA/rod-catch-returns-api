package uk.gov.defra.datareturns.data.model.reporting.feeds.catches;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Small catch reporting feed
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_feed_small_catches")
@Getter
@Headers
public class SmallCatchFeed {
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
    @Parsed(field = "Month")
    private Short month;
    @Basic
    @Parsed(field = "Species ID")
    private Integer speciesId;
    @Basic
    @Parsed(field = "Released")
    private Short released;
}
