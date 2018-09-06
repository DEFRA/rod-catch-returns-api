package uk.gov.defra.datareturns.test.submissions;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;
import uk.gov.defra.datareturns.testcommons.restassured.RestAssuredRule;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Month;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.fromJson;

/**
 * Integration tests submission-level property validation
 */
@RunWith(SpringRunner.class)
@WebIntegrationTest
@Slf4j
public class SubmissionIT {
    @Inject
    @Rule
    public RestAssuredRule restAssuredRule;

    private static ValidatableResponse getEntity(final String entityUrl) {
        return given()
                .when()
                .get(entityUrl)
                .then()
                .log().all();
    }

    private static String createEntity(final String resourceUrl, final String entityJson, final Consumer<ValidatableResponse> responseAssertions) {
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(entityJson)
                .when()
                .post(resourceUrl)
                .then()
                .log().all();
        responseAssertions.accept(response);

        String entityUrl = null;
        if (response != null) {
            entityUrl = response.extract().header("Location");
        }
        return entityUrl;
    }

    private static void deleteEntity(final String url) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private static String getSubmissionJson(final String contactId, final int season) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("CONTACT_ID", contactId);
        replacements.put("SEASON", season);
        return fromJson("/data/templates/submission.json.template", replacements);
    }

    private static String getActivityJson(final String submissionId, final String riverId, final int days) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("SUBMISSION", submissionId);
        replacements.put("RIVER", riverId);
        replacements.put("DAYS", days);
        return fromJson("/data/templates/activity.json.template", replacements);
    }

    private static String getCatchJson(final String submissionId, final String riverId, final String speciesId, final String methodId,
                                       final CatchMass.MeasurementType massType, final BigDecimal mass, final boolean released) {
        final String massProperty = CatchMass.MeasurementType.IMPERIAL.equals(massType) ? "oz" : "kg";
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("SUBMISSION", submissionId);
        replacements.put("SPECIES", speciesId);
        replacements.put("RIVER", riverId);
        replacements.put("METHOD", methodId);
        replacements.put("MASS", mass);
        replacements.put("MASS_TYPE", massType.name());
        replacements.put("MASS_TYPE_PROP", massProperty);
        replacements.put("RELEASED", released);
        return fromJson("/data/templates/catch.json.template", replacements);
    }

    private static String getSmallCatchJson(final String submissionId, final String riverId, final Month month, final Map<String, Integer> counts,
                                            final int released) {
        final String countsJson = counts.entrySet().stream()
                .map((e) -> getSmallCatchCountJson(e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));


        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("SUBMISSION", submissionId);
        replacements.put("RIVER", riverId);
        replacements.put("MONTH", month.name());
        replacements.put("RELEASED", released);
        replacements.put("COUNTS_JSON", countsJson);
        return fromJson("/data/templates/small.catch.json.template", replacements);
    }


    private static String getSmallCatchCountJson(final String methodId, final int count) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("METHOD", methodId);
        replacements.put("COUNT", count);
        return fromJson("/data/templates/small.catch.count.json.template", replacements);
    }

    @Test
    public void testSubmissionJourney() {
        final String submissionJson = getSubmissionJson(RandomStringUtils.randomAlphanumeric(30),
                Calendar.getInstance().get(Calendar.YEAR));

        String submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String activityJson = getActivityJson(submissionUrl, "rivers/1", 5);
        String activityUrl = createEntity("/activities", activityJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String catchJson = getCatchJson(submissionUrl, "rivers/1", "species/1", "methods/1", CatchMass.MeasurementType.METRIC, BigDecimal.ONE,
                false);
        String catchUrl = createEntity("/catches", catchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String smallCatchJson = getSmallCatchJson(submissionUrl, "rivers/1", Month.MARCH, Collections.singletonMap("methods/1", 5), 5);
        String smallCatchUrl = createEntity("/smallCatches", smallCatchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });
        deleteEntity(submissionUrl);
        getEntity(submissionUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(activityUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(catchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(smallCatchUrl).statusCode(HttpStatus.NOT_FOUND.value());
    }
}
