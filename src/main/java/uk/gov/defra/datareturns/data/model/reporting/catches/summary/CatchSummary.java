package uk.gov.defra.datareturns.data.model.reporting.catches.summary;

import com.univocity.parsers.annotations.Headers;
import lombok.Getter;
import uk.gov.defra.datareturns.data.model.reporting.catches.AbstractCatchSummary;

import javax.persistence.Entity;

/**
 * Reporting summary class
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_catch_report")
@Getter
@Headers
public class CatchSummary extends AbstractCatchSummary {
}
