package uk.gov.defra.datareturns.test.reporting;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import java.io.IOException;
import java.io.InputStream;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.getEntity;

/**
 * Integration tests for reporting functionality
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@WithAdminUser
@Slf4j
public class ReportingIT {
    private static RowListProcessor readCsvFromResponse(final ValidatableResponse response) {
        final RowListProcessor rowProcessor = new RowListProcessor();
        try (final InputStream responseContent = response.extract().body().asInputStream()) {
            final CsvParserSettings parserSettings = new CsvParserSettings();
            parserSettings.setHeaderExtractionEnabled(true);

            parserSettings.setProcessor(rowProcessor);
            final CsvParser parser = new CsvParser(parserSettings);
            parser.parse(responseContent);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return rowProcessor;

    }

    @Test
    public void testCatchReporting() {
        final ValidatableResponse response = getEntity("/reporting/catches/2018");
        final RowListProcessor rowProcessor = readCsvFromResponse(response);
        final String[] headers = rowProcessor.getHeaders();

        final String[] expectedHeaders = {
                "Season", "Month", "Region", "Catchment", "River", "Species", "Number Caught", "Total Mass Caught (kg)",
                "Average Catch Mass (kg)", "Largest Catch Mass (kg)", "Smallest Catch Mass (kg)", "Number Released", "Total Mass Released (kg)"
        };
        Assertions.assertThat(headers).isNotNull();
        Assertions.assertThat(headers).hasSize(expectedHeaders.length);
        Assertions.assertThat(headers).containsExactly(expectedHeaders);

        // TODO: Add some row content tests (need to insert a set of known submissions and test reporting values)
        // final List<String[]> rows = rowProcessor.getRows();
    }

    @Test
    public void testCatchByContactReporting() {
        final ValidatableResponse response = getEntity("/reporting/catchesByContact/2018");
        final RowListProcessor rowProcessor = readCsvFromResponse(response);
        final String[] headers = rowProcessor.getHeaders();

        final String[] expectedHeaders = {
                "Contact Id", "Season", "Month", "Region", "Catchment", "River", "Species", "Number Caught", "Total Mass Caught (kg)",
                "Average Catch Mass (kg)", "Largest Catch Mass (kg)", "Smallest Catch Mass (kg)", "Number Released", "Total Mass Released (kg)"
        };
        Assertions.assertThat(headers).isNotNull();
        Assertions.assertThat(headers).hasSize(expectedHeaders.length);
        Assertions.assertThat(headers).containsExactly(expectedHeaders);

        // TODO: Add some row content tests (need to insert a set of known submissions and test reporting values)
        // final List<String[]> rows = rowProcessor.getRows();
    }
}
