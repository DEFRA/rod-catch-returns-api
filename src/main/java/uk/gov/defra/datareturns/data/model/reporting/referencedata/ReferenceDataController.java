package uk.gov.defra.datareturns.data.model.reporting.referencedata;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.reporting.referencedata.locations.LocationEntry;
import uk.gov.defra.datareturns.data.model.reporting.referencedata.locations.LocationEntryRepository;
import uk.gov.defra.datareturns.data.model.species.Species;
import uk.gov.defra.datareturns.data.model.species.SpeciesRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.gov.defra.datareturns.util.CsvUtil.writeCsv;

/**
 * Extensions to the default spring data rest endpoint for grilse probabilities, allows data to be produced and consumed using CSV.
 *
 * @author Sam Gardner-Dell
 */
@BasePathAwareController
@ConditionalOnWebApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reporting/reference")
public class ReferenceDataController implements ResourceProcessor<RepositoryLinksResource> {
    private final LocationEntryRepository locationEntryRepository;
    private final SpeciesRepository speciesRepository;
    private final MethodRepository methodRepository;

    @GetMapping(value = "/locations")
    @ApiOperation(value = "Retrieve location information", produces = "text/csv")
    public void locations(final HttpServletResponse response) throws IOException {
        writeCsv(LocationEntry.class, locationEntryRepository.findAll(), response);
    }

    @GetMapping(value = "/species")
    @ApiOperation(value = "Retrieve the species listing", produces = "text/csv")
    public void species(final HttpServletResponse response) throws IOException {
        writeCsv(Species.class, speciesRepository.findAll(), response);
    }

    @GetMapping(value = "/methods")
    @ApiOperation(value = "Retrieve the methods listing", produces = "text/csv")
    public void methods(final HttpServletResponse response) throws IOException {
        writeCsv(Method.class, methodRepository.findAll(), response);
    }

    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        final String base = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        resource.add(new Link(base + "reporting/reference/locations", "locationsReporting"));
        resource.add(new Link(base + "reporting/reference/species", "speciesReporting"));
        resource.add(new Link(base + "reporting/reference/methods", "methodsReporting"));
        return resource;
    }
}
