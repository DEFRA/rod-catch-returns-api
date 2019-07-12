package uk.gov.defra.datareturns.data.model.reporting.referencedata;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.Value;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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

    private enum ErrorType {
        DUPLICATE_HEADERS,
        OVERWRITE_DISALLOWED,
        COLUMN_DISALLOWED,
        MISSING_REQUIRED,
        DUPLICATE,
        INVALID_CSV,
        NOT_WHOLE_NUMBER,
        ROW_HEADER_DISCREPANCY,
        INVALID_PROBABILITY
    }

    @Value(staticConstructor = "of")
    private final static class ErrorResultSet {
        final ErrorType errorType;
        final Map<ErrorType, Set<String>> headerErrors;
        final Map<ErrorType, Map<String, Set<Short>>> errorsByColumnAndRowNumber;
        final Map<ErrorType, Set<Short>> errorsByRow;
    }

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
                return new ResponseEntity<>(ErrorResultSet.of(ErrorType.OVERWRITE_DISALLOWED, null, null, null), HttpStatus.CONFLICT);
            }
            grilseProbabilityRepository.deleteAll(existing);
        }

        return loader.transform(season, grilseProbabilityRepository);
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
        private int numberOfHeaders = 0;

        public GrilseDataLoader(final InputStream stream) {
            // Read the csv data into a set of GrilseProbability beans and then set the season from the request path
            data = read(stream);
        }

        private ResponseEntity<Object> transform(final Short season, final GrilseProbabilityRepository grilseProbabilityRepository) {
            try {
                final List<GrilseProbability> grilseProbabilities = new ArrayList<>();
                final Set<Short> weightsProcessed = new HashSet<>();
                final Set<String> monthNames = Arrays.stream(Month.values()).map(Month::name).collect(Collectors.toSet());
                final Map<ErrorType, Set<String>> headerErrors = new HashMap<>();
                final Map<ErrorType, Map<String, Set<Short>>> errorsByColumnAndRowNumber = new HashMap<>();
                final Map<ErrorType, Set<Short>> errorsByRow = new HashMap<>();

                grilseProbabilityRepository.flush();

                // Test for duplicate headers
                final Map<String, Long> headerCounts = Arrays.stream(data.getHeaders())
                        .map(String::toLowerCase)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                final Set<String> duplicates = headerCounts.entrySet().stream()
                        .filter(e -> e.getValue() > 1)
                        .map(Map.Entry::getKey)
                        .map(StringUtils::capitalize)
                        .collect(Collectors.toSet());

                if (duplicates.size() != 0) {
                    headerErrors.put(ErrorType.DUPLICATE_HEADERS, duplicates);
                }

                this.numberOfHeaders = data.getHeaders().length;

                // Parse headers to determine the appropriate column indexes from which to extract data
                for (int i = 0; i < data.getHeaders().length; i++) {
                    final String headerVal = data.getHeaders()[i];
                    final String header = StringUtils.upperCase(headerVal);
                    if (monthNames.contains(header)) {
                        this.monthFieldIndexes.put(Month.valueOf(header), i);
                    } else if ("WEIGHT".equals(header)) {
                        this.weightColumnIndex = i;
                    } else {
                        // Encountered a header that was not recognised
                        headerErrors.put(ErrorType.COLUMN_DISALLOWED, Collections.singleton(headerVal));
                    }
                }

                // If we couldn't find  "WEIGHT" column and at least one month heading then return an error
                if (this.weightColumnIndex < 0) {
                    headerErrors.put(ErrorType.MISSING_REQUIRED, Collections.singleton("WEIGHT"));
                }

                if (this.monthFieldIndexes.isEmpty()) {
                    headerErrors.put(ErrorType.MISSING_REQUIRED, Collections.singleton("<MONTH>"));
                }

                // Cannot process the rows unless the headers are ok so return here if errors
                if (!headerErrors.isEmpty()) {
                    return new ResponseEntity<>(ErrorResultSet.of(null, headerErrors, null, null), HttpStatus.BAD_REQUEST);
                } else {

                    // Row counter
                    short rownum = 0;


                    for (final Object[] rowData : data.getRows()) {
                        rownum++;

                        // Java - lambda requires effective final
                        final short rownuml = rownum;

                        // Check the number of data items in the row
                        if (this.numberOfHeaders != rowData.length) {
                            if (errorsByRow.containsKey(ErrorType.ROW_HEADER_DISCREPANCY)) {
                                errorsByRow.get(ErrorType.ROW_HEADER_DISCREPANCY).add(rownum);
                            } else {
                                errorsByRow.put(ErrorType.ROW_HEADER_DISCREPANCY, new HashSet<>(Collections.singletonList(rownum)));
                            }
                        }

                        /*
                         * Extract the weight (in lbs) that this row of data belongs to and check that it isn't duplicated from a previously processed
                         * row and that it is a short.
                         */
                        try {
                            short weightVal = Short.parseShort(Objects.toString(rowData[weightColumnIndex]));
                            if (!weightsProcessed.add(weightVal)) {
                                gatherRowError(ErrorType.DUPLICATE, "WEIGHT", rownum, errorsByColumnAndRowNumber);
                            }

                            // For each month column that was discovered, extract the probability.
                            monthFieldIndexes.forEach((month, fieldIndex) -> {
                                // Only add a grilse probability value if the probability is between zero and 1)
                                final BigDecimal probability = (fieldIndex < rowData.length && rowData[fieldIndex] != null) ? new BigDecimal(
                                        Objects.toString(rowData[fieldIndex])) : BigDecimal.ZERO;

                                if (probability.compareTo(BigDecimal.ZERO) >= 0 && probability.compareTo(BigDecimal.ONE) <= 0) {
                                    if (probability.compareTo(BigDecimal.ZERO) > 0) {
                                        grilseProbabilities.add(GrilseProbability.of(null, season, (short) month.getValue(), weightVal, probability));
                                    }
                                } else {
                                    gatherRowError(ErrorType.INVALID_PROBABILITY, month.name(), rownuml, errorsByColumnAndRowNumber);
                                }
                            });

                        } catch (NumberFormatException e) {
                            gatherRowError(ErrorType.NOT_WHOLE_NUMBER, "WEIGHT", rownum, errorsByColumnAndRowNumber);
                        }
                    }

                    if (errorsByColumnAndRowNumber.isEmpty() && errorsByRow.isEmpty()) {
                        grilseProbabilities.sort(Comparator.comparingInt(GrilseProbability::getMassInPounds).thenComparing(GrilseProbability::getMonth));
                        grilseProbabilityRepository.saveAll(grilseProbabilities);
                        return new ResponseEntity<>(HttpStatus.CREATED);
                    } else {
                        return new ResponseEntity<>(ErrorResultSet.of(null,null, errorsByColumnAndRowNumber, errorsByRow), HttpStatus.BAD_REQUEST);
                    }
                }
            } catch (NullPointerException e) {
                return new ResponseEntity<>(ErrorResultSet.of(ErrorType.INVALID_CSV, null, null, null), HttpStatus.BAD_REQUEST);
            }
        }
    }

    private static void gatherRowError(final ErrorType errorType,
                                       final String colName,
                                       final short rownum,
                                       final Map<ErrorType, Map<String, Set<Short>>> errorsByRowNumber) {
        if (errorsByRowNumber.containsKey(errorType)) {
            final Map<String, Set<Short>> err = errorsByRowNumber.get(errorType);
            if (err.containsKey(colName)) {
                err.get(colName).add(rownum);
            } else {
                err.put(colName, new HashSet<>(Collections.singletonList(rownum)));
            }
        } else {
            final Map<String, Set<Short>> err = new HashMap<>();
            err.put(colName, new HashSet<>(Collections.singletonList(rownum)));
            errorsByRowNumber.put(errorType, err);
        }
    }
}
