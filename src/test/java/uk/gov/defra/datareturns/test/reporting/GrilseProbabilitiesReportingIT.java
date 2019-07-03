package uk.gov.defra.datareturns.test.reporting;

import io.restassured.filter.log.LogDetail;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
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

    @Test
    public void testLoad() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/valid-grilse-data-69-datapoints.csv", StandardCharsets.UTF_8);
        final ValidatableResponse postResponse = given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());

        ValidatableResponse getResponse = getEntity("reporting/reference/grilse-probabilities/2018");
        CsvUtil.CsvReadResult<Object[]> result = readCsvFromResponse(getResponse);

        Assertions.assertThat(result.getHeaders()).containsExactly("Season", "Month", "Mass (lbs)", "Probability");
        Assertions.assertThat(result.getRows()).hasSize(62);
        // June should have 6 probability values for 0-6lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "6".equals(s[1])).hasSize(5)
                .extracting(s -> s[2]).containsExactly("1", "2", "3", "4", "5");

        // July should have 9 probability values for 0-8lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "7".equals(s[1])).hasSize(8)
                .extracting(s -> s[2]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8");
        // August should have 10 probability values for 0-9lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "8".equals(s[1])).hasSize(9)
                .extracting(s -> s[2]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9");
        // September should have 12 probability values for 0-11lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "9".equals(s[1])).hasSize(11)
                .extracting(s -> s[2]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        // October should have 12 probability values for 0-11lbs
        Assertions.assertThat(result.getRows()).filteredOn(s -> "10".equals(s[1])).hasSize(11)
                .extracting(s -> s[2]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        // November should have 11 probability values for 0-11lbs (with no value for 10lbs)
        Assertions.assertThat(result.getRows()).filteredOn(s -> "11".equals(s[1])).hasSize(10)
                .extracting(s -> s[2]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "11");
        // December should have 9 probability values for 0-9lbs (with no value for 3lbs)
        Assertions.assertThat(result.getRows()).filteredOn(s -> "12".equals(s[1])).hasSize(8)
                .extracting(s -> s[2]).containsExactly("1", "2", "4", "5", "6", "7", "8", "9");


        getResponse = getEntity("reporting/reference/grilse-probabilities/2017");
        result = readCsvFromResponse(getResponse);
        Assertions.assertThat(result.getHeaders()).containsExactly("Season", "Month", "Mass (lbs)", "Probability");
        Assertions.assertThat(result.getRows()).hasSize(0);
    }

    @Test
    public void testInvalidHeaders() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/invalid-headers.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(400))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message", Matchers.equalTo("Unexpected header \"Unknown header\" in grilse probability data"));
    }

    @Test
    public void testNoWeightHeader() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/no-weight-heading.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(400))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message",
                        Matchers.equalTo("Unexpected/incorrect headings found:  Must contain a weight heading and at least one month heading"));
    }

    @Test
    public void testNoMonthHeader() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/no-month-headings.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message",
                        Matchers.equalTo("Unexpected/incorrect headings found:  Must contain a weight heading and at least one month heading"));
    }

    @Test
    public void testDuplicateWeight() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/duplicate-weight.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message", Matchers.equalTo("More than one row was found with the same weight value in the weight column, row 3"));
    }

    @Test
    public void testOverwrite() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/valid-grilse-data-69-datapoints.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());

        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CONFLICT.value())
                .body("status", Matchers.equalTo(HttpStatus.CONFLICT.value()))
                .body("error", Matchers.equalTo("Conflict"))
                .body("message", Matchers.equalTo("Existing data found for the season \"2018\" but overwrite parameter not set"));

        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018?overwrite=true")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    public void testDuplicateHeaders() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/duplicate-headers.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(400))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message", Matchers.equalTo("Duplicated headers \"June, August\" in grilse probability data"));
    }

    @Test
    public void testWeightIsWhole() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/weight-not-whole-number.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(400))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message", Matchers.equalTo("Found weights that are not whole numbers in the weight column, e.g 2.1 on row 2"));
    }

    @Test
    public void testProbabilitiesNotBetweenZeroAndOne() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/probability-not-between-0-and-1.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(400))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message", Matchers.equalTo("Found probabilities not between 0 and 1, e.g -0.001"));
    }

    @Test
    public void wrongNumberOfDataOnRow() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/wrong-number-of-data-on-row.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(400))
                .body("error", Matchers.equalTo("Bad Request"))
                .body("message", Matchers.equalTo("The number of data items on a row: 3 does not match the header"));
    }

    @Test
    public void testMissingProbabilitiesTreatedAsZero() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/missing-probabilities-treated-as-zeros.csv", StandardCharsets.UTF_8);
        final ValidatableResponse postResponse = given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());
    }
}
