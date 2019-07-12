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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
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
                .body("headerErrors.COLUMN_DISALLOWED", hasItems("Unknown header"));
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
                .body("headerErrors.MISSING_REQUIRED", hasItems("WEIGHT"));
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
                .body("headerErrors.MISSING_REQUIRED", hasItems("<MONTH>"));
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
                .body("errorsByColumnAndRowNumber.DUPLICATE.WEIGHT", hasItems(3, 4));
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
                .body("errorType", is("OVERWRITE_DISALLOWED"));

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
                .body("headerErrors.DUPLICATE_HEADERS", hasItems("June", "August"));
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
                .body("errorsByColumnAndRowNumber.NOT_WHOLE_NUMBER.WEIGHT", hasItems(2, 3));
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
                .body("errorsByColumnAndRowNumber.INVALID_PROBABILITY.JUNE", hasItems(2, 3))
                .body("errorsByColumnAndRowNumber.INVALID_PROBABILITY.SEPTEMBER", hasItems(4))
                .body("errorsByColumnAndRowNumber.INVALID_PROBABILITY.DECEMBER", hasItems(3, 4));
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
                .body("errorsByRow.ROW_HEADER_DISCREPANCY", hasItems(3, 4));
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

    @Test
    public void invalidCsv() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/invalid-csv.csv", StandardCharsets.UTF_8);
        final ValidatableResponse postResponse = given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errorType", is("INVALID_CSV"));
    }
}
