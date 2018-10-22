package uk.gov.defra.datareturns.test.methods;

import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;
import uk.gov.defra.datareturns.testutils.WithEndUser;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Calendar;
import java.util.Collections;
import java.util.function.Consumer;

import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.createEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getActivityJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getCatchJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getSmallCatchJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getSubmissionJson;

/**
 * Integration tests submission-level property validation
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@Slf4j
public class MethodIT {
    private static void testMethodAccessiblityFromSubmission(final Consumer<ValidatableResponse> responseAssertions) {
        final String submissionJson = getSubmissionJson(RandomStringUtils.randomAlphanumeric(30),
                Calendar.getInstance().get(Calendar.YEAR));

        final String submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String activityJson = getActivityJson(submissionUrl, "rivers/1", 5, 5);
        final String activityUrl = createEntity("/activities", activityJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String catchJson = getCatchJson(submissionUrl, activityUrl, "species/1", "methods/4",
                CatchMass.MeasurementType.METRIC, BigDecimal.ONE, false);
        createEntity("/catches", catchJson, responseAssertions);


        final String smallCatchJson = getSmallCatchJson(submissionUrl, activityUrl, Month.MARCH, Collections.singletonMap("methods/4", 5), 5);

        createEntity("/smallCatches", smallCatchJson, responseAssertions);
    }

    @Test
    @WithAdminUser
    public void testSecuredMethodAccessibleByAdmin() {
        testMethodAccessiblityFromSubmission((r) -> {
            r.statusCode(HttpStatus.CREATED.value());
        });
    }

    @Test
    @WithEndUser
    public void testSecuredMethodInaccessibleByEndUser() {
        testMethodAccessiblityFromSubmission((r) -> {
            r.statusCode(HttpStatus.FORBIDDEN.value());
            r.body("error", Matchers.equalTo("Access to associated resource denied"));
            r.body("cause", Matchers.startsWith("JSON parse error: Access is denied; nested exception is"));
        });
    }
}
