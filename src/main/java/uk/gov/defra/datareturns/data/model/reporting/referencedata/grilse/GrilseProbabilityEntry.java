package uk.gov.defra.datareturns.data.model.reporting.referencedata.grilse;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import uk.gov.defra.datareturns.data.model.grilse.GrilseProbability;

import java.math.BigDecimal;

@Headers
public class GrilseProbabilityEntry {
    /**
     * The season the probability data relates to
     */
    @Parsed(field = "Season")
    private Short season;

    /**
     * The grilse weight gate that the probability relates to
     */
    @Parsed(field = "Gate")
    private String gate;

    /**
     * The month (1-based index) this probability data relates to
     */
    @Parsed(field = "Month")
    private Short month;

    /**
     * The mass associated with this probability (in imperial pounds, data matched to the nearest whole pound)
     */
    @Parsed(field = "Mass (lbs)")
    private Short massInPounds;

    /**
     * The probability entry (decimal between 0 and 1)
     */
    @Parsed(field = "Probability")
    private BigDecimal probability;

    public GrilseProbabilityEntry(final GrilseProbability entity) {
        this.season = entity.getSeason();
        this.gate = entity.getGate().getName();
        this.month = entity.getMonth();
        this.massInPounds = entity.getMassInPounds();
        this.probability = entity.getProbability();
    }
}
