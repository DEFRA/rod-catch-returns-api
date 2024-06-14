package uk.gov.defra.datareturns.data.model.reporting.referencedata;

import lombok.Getter;
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
import uk.gov.defra.datareturns.data.model.grilse.GrilseWeightGate;
import uk.gov.defra.datareturns.data.model.reporting.filters.SeasonFilter;
import uk.gov.defra.datareturns.data.model.reporting.referencedata.grilse.GrilseProbabilityEntry;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    public void get(@PathVariable("season") final SeasonFilter season, final HttpServletResponse response) throws IOException {
        final Specification<GrilseProbability> seasonSpec = (root, query, cb) -> season.predicate(cb, root.get(GrilseProbability_.season));
        final List<GrilseProbabilityEntry> entries = grilseProbabilityRepository.findAll(Specification.where(seasonSpec))
                .stream().map(GrilseProbabilityEntry::new).collect(Collectors.toList());
        writeCsv(GrilseProbabilityEntry.class, entries, response, "grilse-probabilities-" + season + ".csv");
    }

    @PostMapping(value = "/{season}/{gate}")
    @Transactional
    public ResponseEntity<Object> post(@PathVariable("season") final Short season,
                                       @PathVariable("gate") final GrilseWeightGate gate,
                                       @RequestParam(value = "overwrite", required = false) final boolean overwrite,
                                       final InputStream inputStream) {
        final GrilseCsvParser loader = new GrilseCsvParser(inputStream, season, gate);
        final List<GrilseProbability> existing = grilseProbabilityRepository.findBySeasonAndGate(season, gate);
        if (!existing.isEmpty()) {
            if (!overwrite) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Existing data found for the given season and gate but overwrite parameter not set");
            }
            grilseProbabilityRepository.deleteAll(existing);
        }
        grilseProbabilityRepository.flush();
        grilseProbabilityRepository.saveAll(loader.getGrilseProbabilities());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        final String base = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        resource.add(new Link(base + "reporting/reference/grilse-probabilities/{season}", "grilseProbabilitiesReporting"));
        return resource;
    }

    public enum ErrorType {
        DUPLICATE_HEADERS,
        COLUMN_DISALLOWED,
        MISSING_WEIGHT_HEADER,
        MISSING_MONTH_HEADER,
        DUPLICATE_WEIGHT,
        NOT_WHOLE_NUMBER,
        ROW_HEADER_DISCREPANCY,
        INVALID_PROBABILITY
    }

    public static final class GrilseCsvParser {
        private static final Set<String> MONTH_NAMES = Arrays.stream(Month.values()).map(Month::name).collect(Collectors.toSet());
        private final CsvUtil.CsvReadResult<Object[]> data;
        private final Map<Month, Integer> monthFieldIndexes = new LinkedHashMap<>();
        private final GrilseWeightGate grilseWeightGate;
        private final Short season;
        private Integer weightColumnIndex = null;

        @Getter
        private List<GrilseProbability> grilseProbabilities = null;

        GrilseCsvParser(final InputStream stream, final Short season, final GrilseWeightGate grilseWeightGate) {
            this.data = read(stream);
            this.season = season;
            this.grilseWeightGate = grilseWeightGate;
            parseHeaders();
            this.grilseProbabilities = parseRows();
        }

        private void parseHeaders() {
            final List<GrilseCsvError> errors = new ArrayList<>();
            // Parse headers to determine the appropriate column indexes from which to extract data
            if (data.getHeaders() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "File is empty or not a valid csv.");
            }

            final Set<String> visitedHeaders = new HashSet<>();
            for (int i = 0; i < data.getHeaders().length; i++) {
                final String headerKey = StringUtils.upperCase(data.getHeaders()[i]);
                if (MONTH_NAMES.contains(headerKey)) {
                    this.monthFieldIndexes.put(Month.valueOf(headerKey), i);
                } else if ("WEIGHT".equals(headerKey)) {
                    this.weightColumnIndex = i;
                } else {
                    // Encountered a header that was not recognised
                    errors.add(new GrilseCsvError(ErrorType.COLUMN_DISALLOWED, 0, i));
                }
                // Check for duplicated header
                if (!visitedHeaders.add(headerKey)) {
                    errors.add(new GrilseCsvError(ErrorType.DUPLICATE_HEADERS, 0, i));
                }
            }

            // If we couldn't find "WEIGHT" column and at least one month heading then return an error
            if (this.weightColumnIndex == null) {
                errors.add(new GrilseCsvError(ErrorType.MISSING_WEIGHT_HEADER, 0, data.getHeaders().length));
            }
            if (this.monthFieldIndexes.isEmpty()) {
                errors.add(new GrilseCsvError(ErrorType.MISSING_MONTH_HEADER, 0, data.getHeaders().length));
            }
            // Cannot process the rows unless the headers are ok so return here if errors
            if (!errors.isEmpty()) {
                throw new GrilseCsvException(HttpStatus.BAD_REQUEST, errors);
            }
        }

        private List<GrilseProbability> parseRows() {
            final List<GrilseCsvError> errors = new ArrayList<>();
            final List<GrilseProbability> probabilities = new ArrayList<>();
            final Set<Short> weightsProcessed = new HashSet<>();
            for (int rowIndex = 0; rowIndex < data.getRows().size(); rowIndex++) {
                final Object[] rowData = data.getRows().get(rowIndex);
                final int rowNum = rowIndex + 1;

                if (data.getHeaders().length != rowData.length) {
                    final int col = Math.min(data.getHeaders().length, rowData.length);
                    errors.add(new GrilseCsvError(ErrorType.ROW_HEADER_DISCREPANCY, rowNum, col));
                    continue;
                }

                // Extract the weight (in lbs) that this row of data belongs to and check that it isn't duplicated from a previously processed row
                final String weightField = Objects.toString(rowData[weightColumnIndex]);
                short weightVal = 0;
                if (!StringUtils.isNumeric(weightField)) {
                    errors.add(new GrilseCsvError(ErrorType.NOT_WHOLE_NUMBER, rowNum, weightColumnIndex));
                } else {
                    weightVal = Short.parseShort(weightField);
                    if (!weightsProcessed.add(weightVal)) {
                        errors.add(new GrilseCsvError(ErrorType.DUPLICATE_WEIGHT, rowNum, weightColumnIndex));
                    }
                }

                // For each month column that was discovered, extract the probability.
                for (final Map.Entry<Month, Integer> entry : monthFieldIndexes.entrySet()) {
                    final Month month = entry.getKey();
                    final Integer fieldIndex = entry.getValue();
                    BigDecimal ratio = null;
                    final String strVal = Objects.toString(rowData[fieldIndex], "0");
                    final BigDecimal value = new BigDecimal(strVal);
                    if (isValidWeightRatio(value)) {
                        ratio = value;
                    } else {
                        errors.add(new GrilseCsvError(ErrorType.INVALID_PROBABILITY, rowNum, fieldIndex));
                    }

                    // Only add a grilse probability value if the probability is greater than zero (reporting assumes 0 for any missing data point)
                    if (isDefined(ratio)) {
                        probabilities.add(GrilseProbability.of(null, season, grilseWeightGate, (short) month.getValue(), weightVal, ratio));
                    }
                }
            }
            if (!errors.isEmpty()) {
                throw new GrilseCsvException(HttpStatus.BAD_REQUEST, errors);
            }
            probabilities.sort(Comparator.comparingInt(GrilseProbability::getMassInPounds).thenComparing(GrilseProbability::getMonth));
            return probabilities;
        }

        public boolean isValidWeightRatio(final BigDecimal ratio) {
            return ratio.compareTo(BigDecimal.ZERO) >= 0 && ratio.compareTo(BigDecimal.ONE) < 1;
        }

        public boolean isDefined(final BigDecimal ratio) {
            return ratio != null && ratio.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    @Getter
    static final class GrilseCsvException extends ResponseStatusException {
        private final transient List<GrilseCsvError> errors;

        GrilseCsvException(final HttpStatus status, final List<GrilseCsvError> errors) {
            super(status, "Invalid CSV data");
            this.errors = errors;
        }
    }

    @Getter
    static final class GrilseCsvError {
        private final ErrorType errorType;
        private final int row;
        private final int col;

        GrilseCsvError(final ErrorType errorType, final int rowIndex, final int columnIndex) {
            this.errorType = errorType;
            this.row = rowIndex + 1;
            this.col = columnIndex + 1;
        }
    }
}
