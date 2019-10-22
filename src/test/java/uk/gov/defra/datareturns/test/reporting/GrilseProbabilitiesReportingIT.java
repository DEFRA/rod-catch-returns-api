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
import static org.hamcrest.Matchers.equalTo;
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
    public static final String GRILSE_PROBABILITIES_2018_1 = "reporting/reference/grilse-probabilities/2018/1";

    @Inject
    private GrilseProbabilityRepository grilseProbabilityRepository;

    @Test
    public void testLoad() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/valid-grilse-data-69-datapoints.csv", StandardCharsets.UTF_8);
        final ValidatableResponse postResponse = given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());

        ValidatableResponse getResponse = getEntity("reporting/reference/grilse-probabilities/2018");
        CsvUtil.CsvReadResult<Object[]> result = readCsvFromResponse(getResponse);

        Assertions.assertThat(result.getHeaders()).containsExactly("Season", "Gate", "Month", "Mass (lbs)", "Probability");
        Assertions.assertThat(result.getRows()).hasSize(62);
        final int monthIndex = 2;
        final int massIndex = 3;
        // June should have 6 probability values for 0-6lbs
        Assertions.assertThat(result.getRows()).filteredOn(row -> "6".equals(row[monthIndex])).hasSize(5)
                .extracting(s -> s[massIndex]).containsExactly("1", "2", "3", "4", "5");

        // July should have 9 probability values for 0-8lbs
        Assertions.assertThat(result.getRows()).filteredOn(row -> "7".equals(row[monthIndex])).hasSize(8)
                .extracting(s -> s[massIndex]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8");
        // August should have 10 probability values for 0-9lbs
        Assertions.assertThat(result.getRows()).filteredOn(row -> "8".equals(row[monthIndex])).hasSize(9)
                .extracting(s -> s[massIndex]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9");
        // September should have 12 probability values for 0-11lbs
        Assertions.assertThat(result.getRows()).filteredOn(row -> "9".equals(row[monthIndex])).hasSize(11)
                .extracting(s -> s[massIndex]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        // October should have 12 probability values for 0-11lbs
        Assertions.assertThat(result.getRows()).filteredOn(row -> "10".equals(row[monthIndex])).hasSize(11)
                .extracting(s -> s[massIndex]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        // November should have 11 probability values for 0-11lbs (with no value for 10lbs)
        Assertions.assertThat(result.getRows()).filteredOn(row -> "11".equals(row[monthIndex])).hasSize(10)
                .extracting(s -> s[massIndex]).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "11");
        // December should have 9 probability values for 0-9lbs (with no value for 3lbs)
        Assertions.assertThat(result.getRows()).filteredOn(row -> "12".equals(row[monthIndex])).hasSize(8)
                .extracting(s -> s[massIndex]).containsExactly("1", "2", "4", "5", "6", "7", "8", "9");


        getResponse = getEntity("reporting/reference/grilse-probabilities/2017");
        result = readCsvFromResponse(getResponse);
        Assertions.assertThat(result.getHeaders()).containsExactly("Season", "Gate", "Month", "Mass (lbs)", "Probability");
        Assertions.assertThat(result.getRows()).hasSize(0);
    }

    @Test
    public void testInvalidHeaders() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/invalid-headers.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("errors[0].errorType", equalTo("COLUMN_DISALLOWED"))
                .body("errors[0].row", equalTo(1))
                .body("errors[0].col", equalTo(9));
    }

    @Test
    public void testNoWeightHeader() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/no-weight-heading.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("errors[0].errorType", equalTo("MISSING_WEIGHT_HEADER"))
                .body("errors[0].row", equalTo(1))
                .body("errors[0].col", equalTo(8));
    }

    @Test
    public void testNoMonthHeader() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/no-month-headings.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("errors[0].errorType", equalTo("MISSING_MONTH_HEADER"))
                .body("errors[0].row", equalTo(1))
                .body("errors[0].col", equalTo(2));
    }

    @Test
    public void testDuplicateWeight() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/duplicate-weight.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("errors[0].errorType", equalTo("DUPLICATE_WEIGHT"))
                .body("errors[0].row", equalTo(4))
                .body("errors[0].col", equalTo(1))
                .body("errors[1].errorType", equalTo("DUPLICATE_WEIGHT"))
                .body("errors[1].row", equalTo(5))
                .body("errors[1].col", equalTo(1));
    }

    @Test
    public void testOverwrite() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/valid-grilse-data-69-datapoints.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());

        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CONFLICT.value())
                .body("status", Matchers.equalTo(HttpStatus.CONFLICT.value()))
                .body("error", Matchers.equalTo("Conflict"))
                .body("message", Matchers.equalTo("Existing data found for the given season and gate but overwrite parameter not set"));

        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1 + "?overwrite=true")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());

        // The gate source sets are independent
        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018/2")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());

        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018/2")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CONFLICT.value())
                .body("status", Matchers.equalTo(HttpStatus.CONFLICT.value()))
                .body("error", Matchers.equalTo("Conflict"))
                .body("message", Matchers.equalTo("Existing data found for the given season and gate but overwrite parameter not set"));

        given().contentType("text/csv").body(csvData)
                .when().post("reporting/reference/grilse-probabilities/2018/2?overwrite=true")
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    public void testInvalidCsv() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/invalid-csv.csv", StandardCharsets.UTF_8);
        final ValidatableResponse postResponse = given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .body("status", Matchers.equalTo(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .body("error", equalTo("Unprocessable Entity"))
                .body("message", equalTo("File is empty or not a valid csv."));
    }

    @Test
    public void testDuplicateHeaders() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/duplicate-headers.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", Matchers.equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("errors[0].errorType", equalTo("DUPLICATE_HEADERS"))
                .body("errors[0].row", equalTo(1))
                .body("errors[0].col", equalTo(3))
                .body("errors[1].errorType", equalTo("DUPLICATE_HEADERS"))
                .body("errors[1].row", equalTo(1))
                .body("errors[1].col", equalTo(6));
    }

    @Test
    public void testWeightIsWhole() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/weight-not-whole-number.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors[0].errorType", equalTo("NOT_WHOLE_NUMBER"))
                .body("errors[0].row", equalTo(3))
                .body("errors[0].col", equalTo(1))
                .body("errors[1].errorType", equalTo("NOT_WHOLE_NUMBER"))
                .body("errors[1].row", equalTo(4))
                .body("errors[1].col", equalTo(1));
    }

    @Test
    public void testProbabilitiesNotBetweenZeroAndOne() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/probability-not-between-0-and-1.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors[0].errorType", equalTo("INVALID_PROBABILITY"))
                .body("errors[0].row", equalTo(3))
                .body("errors[0].col", equalTo(2))
                .body("errors[1].errorType", equalTo("INVALID_PROBABILITY"))
                .body("errors[1].row", equalTo(4))
                .body("errors[1].col", equalTo(2))
                .body("errors[2].errorType", equalTo("INVALID_PROBABILITY"))
                .body("errors[2].row", equalTo(4))
                .body("errors[2].col", equalTo(8))
                .body("errors[3].errorType", equalTo("INVALID_PROBABILITY"))
                .body("errors[3].row", equalTo(5))
                .body("errors[3].col", equalTo(5))
                .body("errors[4].errorType", equalTo("INVALID_PROBABILITY"))
                .body("errors[4].row", equalTo(5))
                .body("errors[4].col", equalTo(8));
    }

    @Test
    public void testInvalidRowLength() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/wrong-number-of-data-on-row.csv", StandardCharsets.UTF_8);
        given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("errors[0].errorType", equalTo("ROW_HEADER_DISCREPANCY"))
                .body("errors[0].row", equalTo(4))
                .body("errors[0].col", equalTo(8))
                .body("errors[1].errorType", equalTo("ROW_HEADER_DISCREPANCY"))
                .body("errors[1].row", equalTo(5))
                .body("errors[1].col", equalTo(9));
    }

    @Test
    public void testMissingProbabilitiesTreatedAsZero() throws IOException {
        grilseProbabilityRepository.deleteAll();
        final String csvData = IOUtils.resourceToString("/data/grilse/missing-probabilities-treated-as-zeros.csv", StandardCharsets.UTF_8);
        final ValidatableResponse postResponse = given().contentType("text/csv").body(csvData)
                .when().post(GRILSE_PROBABILITIES_2018_1)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.CREATED.value());
    }
}
