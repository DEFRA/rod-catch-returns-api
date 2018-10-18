package uk.gov.defra.datareturns.testutils;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Condition;
import org.springframework.http.HttpStatus;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;


/**
 * Test utilities for submission
 *
 * @author Sam Gardner-Dell
 */
public final class SubmissionTestUtils {

    private SubmissionTestUtils() {
    }

    public static String fromJson(final String path) {
        try {
            return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fromJson(final String path, final Map<String, Object> templateValues) {
        String json = fromJson(path);
        for (final Map.Entry<String, Object> entry : templateValues.entrySet()) {
            json = json.replaceAll("%" + entry.getKey() + "%", String.valueOf(entry.getValue()));
        }
        return json;
    }

    public static Condition<ConstraintViolation<?>> violationMessageMatching(final String expectedMessage) {
        return new Condition<ConstraintViolation<?>>() {
            @Override
            public boolean matches(final ConstraintViolation<?> value) {
                return expectedMessage.equals(value.getMessage());
            }
        };
    }

    public static ValidatableResponse getEntity(final String entityUrl) {
        return given()
                .when()
                .get(entityUrl)
                .then()
                .log().status().log().body();
    }

    public static String createEntity(final String resourceUrl, final String entityJson, final Consumer<ValidatableResponse> responseAssertions) {
        final ValidatableResponse response = given()
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

    public static void deleteEntity(final String url) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    public static String getSubmissionJson(final String contactId, final int season) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("CONTACT_ID", contactId);
        replacements.put("SEASON", season);
        return fromJson("/data/templates/submission.json.template", replacements);
    }

    public static String getActivityJson(final String submissionId, final String riverId, final int daysFishedWithMandatoryRelease,
                                         final int daysFishedOther) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("SUBMISSION", submissionId);
        replacements.put("RIVER", riverId);
        replacements.put("DAYS_FISHED_WITH_MANDATORY_RELEASE", daysFishedWithMandatoryRelease);
        replacements.put("DAYS_FISHED_OTHER", daysFishedOther);
        return fromJson("/data/templates/activity.json.template", replacements);
    }

    public static String getCatchJson(final String submissionId, final String activityId, final String speciesId, final String methodId,
                                      final CatchMass.MeasurementType massType, final BigDecimal mass, final boolean released) {
        final String massProperty = CatchMass.MeasurementType.IMPERIAL.equals(massType) ? "oz" : "kg";
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("SUBMISSION", submissionId);
        replacements.put("SPECIES", speciesId);
        replacements.put("ACTIVITY", activityId);
        replacements.put("METHOD", methodId);
        replacements.put("MASS", mass);
        replacements.put("MASS_TYPE", massType.name());
        replacements.put("MASS_TYPE_PROP", massProperty);
        replacements.put("RELEASED", released);
        return fromJson("/data/templates/catch.json.template", replacements);
    }

    public static String getSmallCatchJson(final String submissionId, final String activityId, final Month month, final Map<String, Integer> counts,
                                           final int released) {
        final String countsJson = counts.entrySet().stream()
                .map((e) -> getSmallCatchCountJson(e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));


        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("SUBMISSION", submissionId);
        replacements.put("ACTIVITY", activityId);
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
}
