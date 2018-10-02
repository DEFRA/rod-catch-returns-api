package uk.gov.defra.datareturns.data.model.reporting.catches;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.math.BigDecimal;

/**
 * Reporting summary class
 *
 * @author Sam Gardner-Dell
 */
@MappedSuperclass
@Getter
@Setter
@Headers
public abstract class AbstractCatchSummary {
    @Id
    private String id;
    @Basic
    @Parsed(field = "Season")
    private Short season;
    @Basic
    @Parsed(field = "Month")
    private String month;
    @Basic
    @Parsed(field = "Region")
    private String region;
    @Basic
    @Parsed(field = "Catchment")
    private String catchment;
    @Basic
    @Parsed(field = "River")
    private String river;
    @Basic
    @Parsed(field = "Species")
    private String species;
    @Basic
    @Parsed(field = "Number Caught")
    private Long caught;
    @Basic
    @Parsed(field = "Total Mass Caught (kg)")
    private BigDecimal caughtTotalMass;
    @Basic
    @Parsed(field = "Average Catch Mass (kg)")
    private BigDecimal caughtAvgMass;
    @Basic
    @Parsed(field = "Largest Catch Mass (kg)")
    private BigDecimal caughtMaxMass;
    @Basic
    @Parsed(field = "Smallest Catch Mass (kg)")
    private BigDecimal caughtMinMass;
    @Basic
    @Parsed(field = "Number Released")
    private Long released;
    @Basic
    @Parsed(field = "Total Mass Released (kg)")
    private BigDecimal releasedTotalMass;
}
