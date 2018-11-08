package uk.gov.defra.datareturns.data.model.reporting.catches.bycontact;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;
import uk.gov.defra.datareturns.data.model.reporting.catches.AbstractCatchSummary;

import javax.persistence.Basic;
import javax.persistence.Entity;

/**
 * Reporting summary class
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_catch_report_by_contact")
@Getter
@Headers
public class CatchSummaryByContact extends AbstractCatchSummary {
    @Basic
    @Parsed(field = "Contact Id")
    private String contactId;
}
