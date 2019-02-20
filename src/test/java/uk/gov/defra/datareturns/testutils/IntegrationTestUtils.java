package uk.gov.defra.datareturns.testutils;

import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Condition;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

import static io.restassured.RestAssured.given;


/**
 * Test utilities for submissions
 *
 * @author Sam Gardner-Dell
 */
public final class IntegrationTestUtils {

    private IntegrationTestUtils() {
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
                .log().ifValidationFails(LogDetail.ALL);
    }

    public static String createEntity(final String resourceUrl, final String entityJson, final Consumer<ValidatableResponse> responseAssertions) {
        final ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(entityJson)
                .when()
                .post(resourceUrl)
                .then()
                .log().ifValidationFails(LogDetail.ALL);

        responseAssertions.accept(response);

        String entityUrl = null;
        if (response != null) {
            entityUrl = response.extract().header("Location");
        }
        return entityUrl;
    }

    public static void patchEntity(final String resourceUrl, final String entityJson, final Consumer<ValidatableResponse> responseAssertions) {
        final ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(entityJson)
                .when()
                .patch(resourceUrl)
                .then()
                .log().ifValidationFails(LogDetail.ALL);
        responseAssertions.accept(response);
    }

    public static void deleteEntity(final String url) {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .log().ifValidationFails(LogDetail.ALL)
                .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
