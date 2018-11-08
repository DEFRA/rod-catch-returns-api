package uk.gov.defra.datareturns.test.submissions;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.services.crm.DynamicsMockData;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithEndUser;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.ActivityDef;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.createActivities;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.createCatches;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.createEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.createSmallCatches;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.deleteEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getActivityJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getCatchJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getSmallCatchJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.getSubmissionJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.patchEntity;

/**
 * Integration tests submission-level property validation
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@WithEndUser
@Slf4j
public class SubmissionIT {
    @Test
    public void testSubmissionJourney() {
        // Create the submission
        final String submissionJson = getSubmissionJson(DynamicsMockData.get(1).getContactId(),
                Calendar.getInstance().get(Calendar.YEAR));

        final String submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        // Create some activities
        final List<String> activities = createActivities(submissionUrl,
                ActivityDef.of("rivers/3", 20, 5),
                ActivityDef.of("rivers/5", 40, 30));
        final Map<String, List<String>> catchesByActivity = new HashMap<>();
        final Map<String, List<String>> smallCatchesByActivity = new HashMap<>();

        for (final String activityUrl : activities) {
            // Create multiple catches
            final List<String> catches = new ArrayList<>();
            catches.addAll(createCatches(submissionUrl, activityUrl, "species/1", "methods/1",
                    Pair.of(CatchMass.MeasurementType.METRIC, BigDecimal.ONE),
                    Pair.of(CatchMass.MeasurementType.IMPERIAL, new BigDecimal(23))));
            catches.addAll(createCatches(submissionUrl, activityUrl, "species/2", "methods/2",
                    Pair.of(CatchMass.MeasurementType.METRIC, new BigDecimal(0.8343434d)),
                    Pair.of(CatchMass.MeasurementType.IMPERIAL, new BigDecimal(45.3434d))));
            catchesByActivity.put(activityUrl, catches);

            // Create small catch entries for every month.
            smallCatchesByActivity.put(activityUrl, createSmallCatches(submissionUrl, activityUrl, 5,
                    Pair.of("methods/1", 2), Pair.of("methods/2", 2), Pair.of("methods/3", 1)));

        }

        // Submit the submission
        final String submissionPatchStr = "{ \"status\": \"SUBMITTED\" }";
        patchEntity(submissionUrl, submissionPatchStr, (r) -> {
            r.statusCode(HttpStatus.OK.value());
            r.body("errors", Matchers.nullValue());
        });


        deleteEntity(submissionUrl);
        activities.forEach(url -> getEntity(url).statusCode(HttpStatus.NOT_FOUND.value()));
        catchesByActivity.values().stream().flatMap(List::stream).forEach(url -> getEntity(url).statusCode(HttpStatus.NOT_FOUND.value()));
        smallCatchesByActivity.values().stream().flatMap(List::stream).forEach(url -> getEntity(url).statusCode(HttpStatus.NOT_FOUND.value()));
        getEntity(submissionUrl).statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testCatchesDeletedWithActivity() {
        final String submissionJson = getSubmissionJson(DynamicsMockData.get(1).getContactId(),
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

        final String catchJson = getCatchJson(submissionUrl, activityUrl, "species/1", "methods/1",
                CatchMass.MeasurementType.METRIC, BigDecimal.ONE, false);
        final String catchUrl = createEntity("/catches", catchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String smallCatchJson = getSmallCatchJson(submissionUrl, activityUrl, Month.MARCH, Collections.singletonMap("methods/1", 5), 5);
        final String smallCatchUrl = createEntity("/smallCatches", smallCatchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        deleteEntity(activityUrl);
        getEntity(catchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(smallCatchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(activityUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(submissionUrl).statusCode(HttpStatus.OK.value());
        deleteEntity(submissionUrl);
    }


    @Test
    public void testDuplicateActivityDetected() {
        final String submissionJson = getSubmissionJson(DynamicsMockData.get(1).getContactId(),
                Calendar.getInstance().get(Calendar.YEAR));

        final String submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String activityJson = getActivityJson(submissionUrl, "rivers/1", 5, 5);
        final String activity1Url = createEntity("/activities", activityJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        createEntity("/activities", activityJson, (r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].message", Matchers.hasToString("ACTIVITY_RIVER_DUPLICATE_FOUND"));
            r.body("errors[0].entity", Matchers.hasToString("Activity"));
        });
        deleteEntity(submissionUrl);
        getEntity(activity1Url).statusCode(HttpStatus.NOT_FOUND.value());
    }
}
