package uk.gov.defra.datareturns.data.model.reporting.referencedata;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.defra.datareturns.data.model.grilse.GrilseProbability;
import uk.gov.defra.datareturns.data.model.grilse.GrilseProbabilityRepository;
import uk.gov.defra.datareturns.data.model.grilse.GrilseProbability_;
import uk.gov.defra.datareturns.data.model.reporting.filters.SeasonFilter;
import uk.gov.defra.datareturns.util.CsvUtil;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.defra.datareturns.util.CsvUtil.read;
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
@RequestMapping("/reporting/reference/grilse-probabilities")
public class GrilseProbabilityController implements ResourceProcessor<RepositoryLinksResource> {
    private final GrilseProbabilityRepository grilseProbabilityRepository;

    @GetMapping(value = "/{season}")
    @ApiOperation(value = "Retrieve grilse probability data for the given season filter", produces = "text/csv")
    public void get(@PathVariable("season") final SeasonFilter season, final HttpServletResponse response) throws IOException {
        final Specification<GrilseProbability> seasonSpec = (root, query, cb) -> season.predicate(cb, root.get(GrilseProbability_.season));
        final List<GrilseProbability> entries = grilseProbabilityRepository.findAll(Specification.where(seasonSpec));
        writeCsv(GrilseProbability.class, entries, response, "grilse-probabilities-" + season + ".csv");
    }

    @PostMapping(value = "/{season}")
    @ApiOperation(value = "Store new grilse probability data for the given season",
                  consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                  produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Object> post(@PathVariable("season") final Short season,
                                       @RequestParam(value = "overwrite", required = false) final boolean overwrite,
                                       final InputStream inputStream) {
        final GrilseDataLoader loader = new GrilseDataLoader(inputStream);
        final List<GrilseProbability> existing = grilseProbabilityRepository.findBySeason(season);
        if (!existing.isEmpty()) {
            if (!overwrite) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Existing data found for the season \"" + season + "\" but overwrite parameter not set");
            }
            grilseProbabilityRepository.deleteAll(existing);
        }

        grilseProbabilityRepository.flush();
        grilseProbabilityRepository.saveAll(loader.transform(season));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        final String base = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        resource.add(new Link(base + "reporting/reference/grilse-probabilities/{season}", "grilseProbabilitiesReporting"));
        return resource;
    }

    private static final class GrilseDataLoader {
        private final CsvUtil.CsvReadResult<Object[]> data;
        private final Map<Month, Integer> monthFieldIndexes = new HashMap<>();
        private int weightColumnIndex = -1;

        public GrilseDataLoader(final InputStream stream) {
            // Read the csv data into a set of GrilseProbability beans and then set the season from the request path
            data = read(stream);

            final Set<String> monthNames = Arrays.stream(Month.values()).map(Month::name).collect(Collectors.toSet());

            // Parse headers to determine the appropriate column indexes from which to extract data
            for (int i = 0; i < data.getHeaders().length; i++) {
                final String headerVal = data.getHeaders()[i];
                final String header = StringUtils.upperCase(headerVal);
                if (monthNames.contains(header)) {
                    this.monthFieldIndexes.put(Month.valueOf(header), i);
                } else if ("WEIGHT".equals(header)) {
                    this.weightColumnIndex = i;
                } else if (!"NUMBER".equals(header)) {
                    // Encountered a header that was not recognised
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected header \"" + headerVal + "\" in grilse probability data");
                }
            }
            // If we couldn't find  "WEIGHT" column and at least one month heading then return an error
            if (this.weightColumnIndex < 0 || this.monthFieldIndexes.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unexpected/incorrect headings found:  Must contain a weight heading and at least one month heading");
            }
        }

        private List<GrilseProbability> transform(final Short season) {
            final List<GrilseProbability> grilseProbabilities = new ArrayList<>();
            final Set<Short> weightsProcessed = new HashSet<>();
            for (final Object[] rowData : data.getRows()) {
                // Extract the weight (in lbs) that this row of data belongs to and check that it isn't duplicated from a previously processed row.
                final Short weightVal = Short.parseShort(Objects.toString(rowData[weightColumnIndex]));
                if (!weightsProcessed.add(weightVal)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "More than one row was found with the same weight value in the weight column");
                }

                // For each month column that was discovered, extract the probability.
                monthFieldIndexes.forEach((month, fieldIndex) -> {
                    final BigDecimal probability = new BigDecimal(Objects.toString(rowData[fieldIndex]));
                    // Only add a grilse probability value if the probability is greater than zero (reporting assumes 0 for any missing data point)
                    if (probability.compareTo(BigDecimal.ZERO) > 0) {
                        // Duplicate the data for weight=1 to weight=0 (to capture values which are rounded down to zero rather than rounded up)
                        if (weightVal == 1 && !weightsProcessed.contains((short) 0)) {
                            grilseProbabilities.add(GrilseProbability.of(null, season, (short) month.getValue(), (short) 0, probability));
                        }
                        grilseProbabilities.add(GrilseProbability.of(null, season, (short) month.getValue(), weightVal, probability));
                    }
                });
            }
            grilseProbabilities.sort(Comparator.comparingInt(GrilseProbability::getMassInPounds).thenComparing(GrilseProbability::getMonth));
            return grilseProbabilities;
        }
    }
}
