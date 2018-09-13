package uk.gov.defra.datareturns.data.model.smallcatches;

import lombok.Getter;
import lombok.Setter;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.validation.smallcatches.ValidSmallCatchCount;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * RCR Small Catch Counts
 *
 * @author Sam Gardner-Dell
 */
@Embeddable
@Getter
@Setter
@ValidSmallCatchCount
public class SmallCatchCount {
    /**
     * The method used (Fly, Spinner, Bait)
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "method_id")
    private Method method;


    /**
     * The number of catches for the given method
     */
    @Column(name = "count")
    private Short count;
}
