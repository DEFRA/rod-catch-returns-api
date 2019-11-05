package uk.gov.defra.datareturns.test.reporting;

import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;
import uk.gov.defra.datareturns.util.CsvUtil;

import java.io.IOException;
import java.io.InputStream;
import java.time.Year;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.getEntity;
import static uk.gov.defra.datareturns.util.CsvUtil.CsvReadResult;

/**
 * Integration tests for reporting functionality
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@WithAdminUser
@Slf4j
public class ReportingIT {
    public static CsvReadResult<Object[]> readCsvFromResponse(final ValidatableResponse response) {
        try (final InputStream stream = response.extract().body().asInputStream()) {
            return CsvUtil.read(stream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSubmissionsFeed() {
        final ValidatableResponse response = getEntity("/reporting/feeds/submissions/" + Year.now().getValue());
        final CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders()).containsExactly("ID", "Contact ID", "Season", "Status", "Source", "Created", "Last Modified");
    }

    @Test
    public void testActivitiesFeed() {
        final ValidatableResponse response = getEntity("/reporting/feeds/activities/" + Year.now().getValue());
        final CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders())
                .containsExactly("ID", "Submission ID", "River ID", "Days Fished (Mandatory Release)", "Days Fished (Other)");
    }

    @Test
    public void testLargeCatchesFeed() {
        final ValidatableResponse response = getEntity("/reporting/feeds/large-catches/" + Year.now().getValue());
        final CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders())
                .containsExactly("ID", "Activity ID", "Date", "Species ID", "Method ID", "Mass (kg)", "Released",
                        "Only Month Recorded", "No Date Recorded");
    }


    @Test
    public void testSmallCatchesFeed() {
        final ValidatableResponse response = getEntity("/reporting/feeds/small-catches/" + Year.now().getValue());
        final CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders()).containsExactly("ID", "Activity ID", "Month", "Species ID", "Released", "No Month Recorded");
    }

    @Test
    public void testSmallCatchCountsFeed() {
        final ValidatableResponse response = getEntity("/reporting/feeds/small-catch-counts/" + Year.now().getValue());
        final CsvReadResult<Object[]> result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders()).containsExactly("Small Catch ID", "Method ID", "Caught");
    }
}
