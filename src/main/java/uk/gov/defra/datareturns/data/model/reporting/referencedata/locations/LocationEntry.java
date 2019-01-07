package uk.gov.defra.datareturns.data.model.reporting.referencedata.locations;

import com.univocity.parsers.annotations.Headers;
import com.univocity.parsers.annotations.Parsed;
import lombok.Getter;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Location data feed for reporting.
 *
 * @author Sam Gardner-Dell
 */
@Entity(name = "rcv_ref_locations")
@Getter
@Headers
public class LocationEntry {
    @Id
    @Basic
    @Parsed(field = "ID")
    private Long id;
    @Basic
    @Parsed(field = "River")
    private String name;
    @Basic
    @Parsed(field = "Catchment")
    private String catchmentName;
    @Basic
    @Parsed(field = "Region")
    private String regionName;
}
