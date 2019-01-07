package uk.gov.defra.datareturns.data.model.reporting.feeds.submissions;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Catch summary information
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_feed_submissions")
@Getter
@Headers
public class SubmissionFeed {
    @Id
    @Parsed(field = "ID")
    private Long id;
    @Basic
    @Parsed(field = "Contact ID")
    private String contactId;
    @Basic
    @Parsed(field = "Season")
    private Short season;
    @Basic
    @Parsed(field = "Status")
    private String status;
    @Basic
    @Parsed(field = "Source")
    private String source;
    @Basic
    @Parsed(field = "Created")
    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    private Date created;
    @Basic
    @Parsed(field = "Last Modified")
    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    private Date lastModified;
}
