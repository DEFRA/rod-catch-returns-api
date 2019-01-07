package uk.gov.defra.datareturns.data.model.reporting.feeds.catches;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Small catch count reporting feed
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_feed_small_catch_counts")
@Getter
@Headers
public class SmallCatchCountFeed {
    @Id
    private String id;
    /**
     * Season (for filtering only), not part of report feed
     */
    @Basic
    private Short season;
    @Basic
    @Parsed(field = "Small Catch ID")
    private Long smallCatchId;
    @Basic
    @Parsed(field = "Method ID")
    private Long methodId;
    @Basic
    @Parsed(field = "Caught")
    private Short count;
}
