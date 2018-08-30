package uk.gov.defra.datareturns.testutils;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Condition;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

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

    public static void runSubmissionTest(final String submissionJson, final Consumer<ValidatableResponse> responseAssertions) {
        ValidatableResponse response = null;

        try {
            response =
                    given()
                            .contentType(ContentType.JSON)
                            .body(submissionJson)
                            .when()
                            .post("/submissions")
                            .then()
                            .log().all();

            responseAssertions.accept(response);
        } finally {
            // If the call above created a submission, then it is deleted again here.
            if (response != null) {
                final String locationHeader = response.extract().header("Location");
                if (locationHeader != null) {
                    given()
                            .contentType(ContentType.JSON)
                            .when()
                            .delete(locationHeader)
                            .then()
                            .log().all()
                            .statusCode(HttpStatus.NO_CONTENT.value());
                }
            }
        }
    }

    public static Condition<ConstraintViolation<?>> violationMessageMatching(final String expectedMessage) {
        return new Condition<ConstraintViolation<?>>() {
            @Override
            public boolean matches(final ConstraintViolation<?> value) {
                return expectedMessage.equals(value.getMessage());
            }
        };
    }
}
