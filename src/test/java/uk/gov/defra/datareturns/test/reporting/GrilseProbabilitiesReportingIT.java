package uk.gov.defra.datareturns.test.reporting;

import io.restassured.filter.log.LogDetail;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.grilse.GrilseProbabilityRepository;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;
import uk.gov.defra.datareturns.util.CsvUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static uk.gov.defra.datareturns.test.reporting.ReportingIT.readCsvFromResponse;
import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.getEntity;

/**
 * Integration tests for grilse probability reference data feeds
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@WithAdminUser
@Slf4j
public class GrilseProbabilitiesReportingIT {
    @Inject
    private GrilseProbabilityRepository grilseProbabilityRepository;


    @Before
    public void loadGrilseData() throws IOException {
        grilseProbabilityRepository.deleteAll();
        String csvData = IOUtils.resourceToString("/data/grilse/Grilse-Probability-Data.csv", StandardCharsets.UTF_8);
        final ValidatableResponse response = given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    public void testLoad() {
        ValidatableResponse response = getEntity("reporting/reference/grilse-probabilities/2018");
        CsvUtil.CsvReadResult<Object[]> result = readCsvFromResponse(response);

        Assertions.assertThat(result.getHeaders()).containsExactly("Season", "Month", "Mass (lbs)", "Probability");
        Assertions.assertThat(result.getRows()).hasSize(69);
        // June should have 6 probability values for 0-6lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "6".equals(s[1])).hasSize(6)
                .extracting(s -> s[2]).containsExactly("0", "1", "2", "3", "4", "5");

        // July should have 9 probability values for 0-8lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "7".equals(s[1])).hasSize(9)
                .extracting(s -> s[2]).containsExactly("0", "1", "2", "3", "4", "5", "6", "7", "8");
        // August should have 10 probability values for 0-9lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "8".equals(s[1])).hasSize(10)
                .extracting(s -> s[2]).containsExactly("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        // September should have 12 probability values for 0-11lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "9".equals(s[1])).hasSize(12)
                .extracting(s -> s[2]).containsExactly("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        // October should have 12 probability values for 0-11lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "10".equals(s[1])).hasSize(12)
                .extracting(s -> s[2]).containsExactly("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        // November should have 11 probability values for 0-11lbs (with no value for 10lbs)
        Assertions.assertThat(result.getRows()).filteredOn(s -> "11".equals(s[1])).hasSize(11)
                .extracting(s -> s[2]).containsExactly("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "11");
        // December should have 9 probability values for 0-9lbs (with no value for 3lbs)
        Assertions.assertThat(result.getRows()).filteredOn(s -> "12".equals(s[1])).hasSize(9)
                .extracting(s -> s[2]).containsExactly("0", "1", "2", "4", "5", "6", "7", "8", "9");


        response = getEntity("reporting/reference/grilse-probabilities/2017");
        result = readCsvFromResponse(response);
        Assertions.assertThat(result.getHeaders()).containsExactly("Season", "Month", "Mass (lbs)", "Probability");
        Assertions.assertThat(result.getRows()).hasSize(0);
    }
}
