package uk.gov.defra.datareturns.data.model.reporting;

import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.defra.datareturns.data.model.reporting.catches.bycontact.CatchSummaryByContact;
import uk.gov.defra.datareturns.data.model.reporting.catches.bycontact.CatchSummaryByContactRepository;
import uk.gov.defra.datareturns.data.model.reporting.catches.summary.CatchSummary;
import uk.gov.defra.datareturns.data.model.reporting.catches.summary.CatchSummaryRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Controller to enable reporting functionality
 *
 * @author Sam Gardner-Dell
 */
@BasePathAwareController
@ConditionalOnWebApplication
@Slf4j
@RequiredArgsConstructor
public class ReportingController implements ResourceProcessor<RepositoryLinksResource> {
    private final CatchSummaryRepository catchSummaryRepository;
    private final CatchSummaryByContactRepository catchSummaryByContactRepository;

    @GetMapping(value = "/reporting/catches/{season}")
    @ApiOperation(value = "Retrieve reporting summary data for the given season", produces = "text/csv")
    public void catches(@PathVariable("season") final String season, final HttpServletResponse response) throws IOException {
        final List<CatchSummary> entries = catchSummaryRepository.findBySeason(Short.valueOf(season));
        writeCsv(CatchSummary.class, entries, "catches-" + season + ".csv", response);
    }

    @GetMapping(value = "/reporting/catchesByContact/{season}")
    @ApiOperation(value = "Retrieve reporting summary data by contact for the given season", produces = "text/csv")
    public void catchesByContact(@PathVariable("season") final String season, final HttpServletResponse response) throws IOException {
        final List<CatchSummaryByContact> entries = catchSummaryByContactRepository.findBySeason(Short.valueOf(season));
        writeCsv(CatchSummaryByContact.class, entries, "catches-by-contact-" + season + ".csv", response);
    }

    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        // TODO: Base path should be preprended to href
        resource.add(new Link("/reporting", "reporting"));
        return resource;
    }

    /**
     * Writes CSV data to the given {@link HttpServletResponse}
     *
     * @param beanClass the class of the source bean whose data should be written
     * @param entries   a {@link List} of beans that should be serialised to CSV
     * @param filename  the filename for the output file
     * @param response  the {@link HttpServletResponse} to write to
     * @param <T>       the generic type of the beans to be written
     * @throws IOException if an IO error occurs
     */
    private <T> void writeCsv(final Class<T> beanClass, final List<T> entries, final String filename, final HttpServletResponse response)
            throws IOException {
        final CsvWriterSettings settings = new CsvWriterSettings();
        settings.setAutoConfigurationEnabled(true);
        settings.setHeaderWritingEnabled(true);
        settings.setRowWriterProcessor(new BeanWriterProcessor<>(beanClass));
        try (final Writer writer = response.getWriter()) {
            response.setHeader("Content-Type", "text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            final CsvWriter csvWriter = new CsvWriter(writer, settings);
            csvWriter.writeHeaders();
            csvWriter.processRecords(entries);
            csvWriter.flush();
            csvWriter.close();
        }
    }
}
