package uk.gov.defra.datareturns.test.submissions;

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
import uk.gov.defra.datareturns.testutils.SubmissionTestUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Month;
import java.util.Calendar;
import java.util.Collections;

import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.createEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.deleteEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getActivityJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getSubmissionJson;

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

        final String catchJson = SubmissionTestUtils
                .getCatchJson(submissionUrl, activityUrl, "species/1", "methods/1", CatchMass.MeasurementType.METRIC, BigDecimal.ONE,
                        false);
        String catchUrl = createEntity("/catches", catchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String smallCatchJson = SubmissionTestUtils
                .getSmallCatchJson(submissionUrl, activityUrl, Month.MARCH, Collections.singletonMap("methods/1", 5), 5);
        String smallCatchUrl = createEntity("/smallCatches", smallCatchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        deleteEntity(submissionUrl);
        getEntity(catchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(smallCatchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(activityUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(submissionUrl).statusCode(HttpStatus.NOT_FOUND.value());
    }


    @Test
    public void testCatchesDeletedWithActivity() {
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

        final String catchJson = SubmissionTestUtils
                .getCatchJson(submissionUrl, activityUrl, "species/1", "methods/1", CatchMass.MeasurementType.METRIC, BigDecimal.ONE,
                        false);
        String catchUrl = createEntity("/catches", catchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String smallCatchJson = SubmissionTestUtils
                .getSmallCatchJson(submissionUrl, activityUrl, Month.MARCH, Collections.singletonMap("methods/1", 5), 5);
        String smallCatchUrl = createEntity("/smallCatches", smallCatchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        deleteEntity(activityUrl);
        getEntity(catchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(smallCatchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(activityUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(submissionUrl).statusCode(HttpStatus.OK.value());
    }
}
