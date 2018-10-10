package uk.gov.defra.datareturns.test.submissions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;
import uk.gov.defra.datareturns.testutils.RcrRestAssuredRule;
import uk.gov.defra.datareturns.testutils.SubmissionTestUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public RcrRestAssuredRule restAssuredRule;

    @Test
    public void testSubmissionJourney() {
        final String submissionJson = getSubmissionJson(RandomStringUtils.randomAlphanumeric(30),
                Calendar.getInstance().get(Calendar.YEAR));

        final String submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        // Create some activities
        final List<String> activities = createActivities(submissionUrl, Pair.of("rivers/3", 20), Pair.of("rivers/5", 40));
        final Map<String, List<String>> catchesByActivity = new HashMap<>();
        final Map<String, List<String>> smallCatchesByActivity = new HashMap<>();

        for (final String activityUrl : activities) {
            // Create multiple catches
            final List<String> catches = new ArrayList<>();
            catches.addAll(createCatches(submissionUrl, activityUrl, "species/1", "methods/1",
                    Pair.of(CatchMass.MeasurementType.METRIC, BigDecimal.ONE),
                    Pair.of(CatchMass.MeasurementType.IMPERIAL, new BigDecimal(23))));
            catches.addAll(createCatches(submissionUrl, activityUrl, "species/2", "methods/3",
                    Pair.of(CatchMass.MeasurementType.METRIC, new BigDecimal(0.8343434d)),
                    Pair.of(CatchMass.MeasurementType.IMPERIAL, new BigDecimal(45.3434d))));
            catchesByActivity.put(activityUrl, catches);

            // Create small catch entries for every month.
            smallCatchesByActivity.put(activityUrl, createSmallCatches(submissionUrl, activityUrl, 5,
                    Pair.of("methods/1", 2), Pair.of("methods/2", 2), Pair.of("methods/3", 1)));

        }

        deleteEntity(submissionUrl);
        activities.forEach(url -> getEntity(url).statusCode(HttpStatus.NOT_FOUND.value()));
        catchesByActivity.values().stream().flatMap(List::stream).forEach(url -> getEntity(url).statusCode(HttpStatus.NOT_FOUND.value()));
        smallCatchesByActivity.values().stream().flatMap(List::stream).forEach(url -> getEntity(url).statusCode(HttpStatus.NOT_FOUND.value()));
        getEntity(submissionUrl).statusCode(HttpStatus.NOT_FOUND.value());
    }

    @SafeVarargs
    private final List<String> createActivities(final String submissionUrl, final Pair<String, Integer>... daysFishedByRiver) {
        final List<String> activities = new ArrayList<>();
        for (final Pair<String, Integer> riverAndDay : daysFishedByRiver) {
            final String activityJson = getActivityJson(submissionUrl, riverAndDay.getFirst(), riverAndDay.getSecond());
            log.info("Creating activity");
            final String activityUrl = createEntity("/activities", activityJson, (r) -> {
                r.statusCode(HttpStatus.CREATED.value());
                r.body("errors", Matchers.nullValue());
            });
            activities.add(activityUrl);
        }
        return activities;
    }

    @SafeVarargs
    final List<String> createCatches(final String submissionUrl, final String activityUrl, final String species, final String method,
                                     final Pair<CatchMass.MeasurementType, BigDecimal>... catchEntries) {

        final List<String> catches = new ArrayList<>();
        for (final Pair<CatchMass.MeasurementType, BigDecimal> catchEntry : catchEntries) {
            final String catchJson = SubmissionTestUtils
                    .getCatchJson(submissionUrl, activityUrl, species, method, catchEntry.getFirst(), catchEntry.getSecond(),
                            false);

            log.info("Creating catch");
            final String catchUrl = createEntity("/catches", catchJson, (r) -> {
                r.statusCode(HttpStatus.CREATED.value());
                r.body("errors", Matchers.nullValue());
            });
            catches.add(catchUrl);
        }
        return catches;
    }

    @SafeVarargs
    final List<String> createSmallCatches(final String submissionUrl, final String activityUrl, final int released,
                                          final Pair<String, Integer>... methodCounts) {

        final List<String> smallCatches = new ArrayList<>();
        final Map<String, Integer> counts = Arrays.stream(methodCounts).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        for (final Month month : Month.values()) {
            final String smallCatchJson = SubmissionTestUtils
                    .getSmallCatchJson(submissionUrl, activityUrl, month, counts, released);


            log.info("Creating small catch");
            final String smallCatchUrl = createEntity("/smallCatches", smallCatchJson, (r) -> {
                r.statusCode(HttpStatus.CREATED.value());
                r.body("errors", Matchers.nullValue());
            });

            smallCatches.add(smallCatchUrl);
        }
        return smallCatches;
    }


    @Test
    public void testCatchesDeletedWithActivity() {
        final String submissionJson = getSubmissionJson(RandomStringUtils.randomAlphanumeric(30),
                Calendar.getInstance().get(Calendar.YEAR));

        final String submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String activityJson = getActivityJson(submissionUrl, "rivers/1", 5);
        final String activityUrl = createEntity("/activities", activityJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String catchJson = SubmissionTestUtils
                .getCatchJson(submissionUrl, activityUrl, "species/1", "methods/1", CatchMass.MeasurementType.METRIC, BigDecimal.ONE,
                        false);
        final String catchUrl = createEntity("/catches", catchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String smallCatchJson = SubmissionTestUtils
                .getSmallCatchJson(submissionUrl, activityUrl, Month.MARCH, Collections.singletonMap("methods/1", 5), 5);
        final String smallCatchUrl = createEntity("/smallCatches", smallCatchJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        deleteEntity(activityUrl);
        getEntity(catchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(smallCatchUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(activityUrl).statusCode(HttpStatus.NOT_FOUND.value());
        getEntity(submissionUrl).statusCode(HttpStatus.OK.value());
    }


    @Test
    public void testDuplicateActivityDetected() {
        final String submissionJson = getSubmissionJson(RandomStringUtils.randomAlphanumeric(30),
                Calendar.getInstance().get(Calendar.YEAR));

        final String submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        final String activityJson = getActivityJson(submissionUrl, "rivers/1", 5);

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
