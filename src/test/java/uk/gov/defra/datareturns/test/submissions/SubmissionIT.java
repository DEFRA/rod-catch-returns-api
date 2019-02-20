package uk.gov.defra.datareturns.test.submissions;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionRepository;
import uk.gov.defra.datareturns.services.crm.DynamicsMockData;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.TestLicences;
import uk.gov.defra.datareturns.testutils.client.TestActivity;
import uk.gov.defra.datareturns.testutils.client.TestCatch;
import uk.gov.defra.datareturns.testutils.client.TestSmallCatch;
import uk.gov.defra.datareturns.testutils.client.TestSubmission;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

/**
 * Integration tests submission-level property validation
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@Slf4j
public class SubmissionIT {
    @Inject
    private SubmissionRepository submissionRepository;

    @Before
    public void setup() {
        submissionRepository.deleteAll();
    }

    @Test
    public void testSubmissionJourney() {
        final String contactId = DynamicsMockData.get(TestLicences.getLicence(1)).getContactId();
        final int season = Year.now().getValue() - 1;
        final TestSubmission sub = TestSubmission.of(contactId, season);

        final TestActivity act = sub.withActivity().river("rivers/3").daysFishedWithMandatoryRelease(20).daysFishedOther(5);
        sub.create();
        act.create();

        // Update the activity
        act.daysFishedOther(10);
        act.update();

        // Add a large catch
        final TestCatch cat = act.withCatch()
                .anyValidCatchDate()
                .method("methods/1")
                .species("species/1")
                .mass(CatchMass.MeasurementType.METRIC, BigDecimal.ONE)
                .released(false);
        cat.create();

        // Add a small catch
        final TestSmallCatch sc = act.withSmallCatch()
                .month(Month.JANUARY)
                .counts(Pair.of("methods/1", 2), Pair.of("methods/2", 2), Pair.of("methods/3", 1))
                .released(1);
        sc.create();

        // Mark the submission as submitted
        sub.status("SUBMITTED");
        sub.update();

        // Delete the submission and expect all related entities to also be deleted
        sub.delete();
        sub.read().statusCode(HttpStatus.NOT_FOUND.value());
        act.read().statusCode(HttpStatus.NOT_FOUND.value());
        cat.read().statusCode(HttpStatus.NOT_FOUND.value());
        sc.read().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testCatchEntriesDeletedWithActivity() {
        final String contactId = DynamicsMockData.get(TestLicences.getLicence(2)).getContactId();
        final int season = Year.now().getValue() - 1;
        final TestSubmission sub = TestSubmission.of(contactId, season);
        final TestActivity act = sub.withActivity().river("rivers/3").daysFishedWithMandatoryRelease(20).daysFishedOther(5);
        final TestCatch cat = act.withCatch()
                .dateCaught(LocalDate.now().withYear(season).minusDays(1))
                .method("methods/1")
                .species("species/1")
                .mass(CatchMass.MeasurementType.METRIC, BigDecimal.ONE)
                .released(false);
        final TestSmallCatch sc = act.withSmallCatch()
                .month(Month.JANUARY)
                .counts(Pair.of("methods/1", 2), Pair.of("methods/2", 2), Pair.of("methods/3", 1))
                .released(1);
        sub.create();
        act.create();
        cat.create();
        sc.create();

        // Delete the activity and expect related catches to also be deleted
        act.delete();
        cat.read().statusCode(HttpStatus.NOT_FOUND.value());
        sc.read().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testDuplicateActivityDetected() {
        final String contactId = DynamicsMockData.get(TestLicences.getLicence(3)).getContactId();
        final int season = Year.now().getValue() - 1;
        final TestSubmission sub = TestSubmission.of(contactId, season);
        final TestActivity act = sub.withActivity().river("rivers/3").daysFishedWithMandatoryRelease(20).daysFishedOther(5);
        final TestActivity dup = sub.withActivity().river("rivers/3").daysFishedWithMandatoryRelease(1).daysFishedOther(2);

        sub.create();
        act.create();
        dup.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].message", Matchers.hasToString("ACTIVITY_RIVER_DUPLICATE_FOUND"));
            r.body("errors[0].entity", Matchers.hasToString("Activity"));
        });

        sub.delete();
    }
}
