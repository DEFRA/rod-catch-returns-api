package uk.gov.defra.datareturns.test.smallcatches;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionRepository;
import uk.gov.defra.datareturns.services.crm.DynamicsMockData;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.TestLicences;
import uk.gov.defra.datareturns.testutils.client.TestActivity;
import uk.gov.defra.datareturns.testutils.client.TestSmallCatch;
import uk.gov.defra.datareturns.testutils.client.TestSubmission;

import javax.inject.Inject;
import java.time.Month;
import java.time.Year;

import static uk.gov.defra.datareturns.testutils.client.TestSmallCatch.Count;

/**
 * Integration tests for small catch api interactions
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@Slf4j
public class SmallCatchIT {
    @Inject
    private SubmissionRepository submissionRepository;

    private TestSubmission sub;
    private TestActivity act;

    @Before
    public void before() {
        submissionRepository.deleteAll();

        final String contactId = DynamicsMockData.get(TestLicences.getLicence(1)).getContactId();
        final int season = Year.now().getValue() - 1;
        sub = TestSubmission.of(contactId, season);
        act = sub.withActivity().river("rivers/3").daysFishedWithMandatoryRelease(20).daysFishedOther(5);
        sub.create();
        act.create();
    }

    @Test
    public void testMonthNullValidation() {
        final TestSmallCatch sc = act.withSmallCatch().month((String) null)
                .counts(Count.of("methods/1", 2), Count.of("methods/2", 2), Count.of("methods/3", 2))
                .released(6);
        sc.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].entity", Matchers.equalTo("SmallCatch"));
            r.body("errors[0].property", Matchers.equalTo("month"));
            r.body("errors[0].invalidValue", Matchers.equalTo(null));
            r.body("errors[0].message", Matchers.equalTo("SMALL_CATCH_MONTH_REQUIRED"));
        });
    }

    @Test
    public void testCountsArrayNullValidation() {
        final TestSmallCatch sc = act.withSmallCatch().month(Month.JANUARY)
                .released(6);
        sc.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].entity", Matchers.equalTo("SmallCatch"));
            r.body("errors[0].property", Matchers.equalTo("counts"));
            r.body("errors[0].invalidValue", Matchers.equalTo(null));
            r.body("errors[0].message", Matchers.equalTo("SMALL_CATCH_COUNTS_REQUIRED"));
        });
    }


    @Test
    public void testReleasedExceedsCaughtValidation() {
        final TestSmallCatch sc = act.withSmallCatch().month(Month.JANUARY)
                .counts(Count.of("methods/1", 2), Count.of("methods/2", 2), Count.of("methods/3", 2))
                .released(7);
        sc.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].entity", Matchers.equalTo("SmallCatch"));
            r.body("errors[0].property", Matchers.equalTo("released"));
            r.body("errors[0].invalidValue", Matchers.equalTo(7));
            r.body("errors[0].message", Matchers.equalTo("SMALL_CATCH_RELEASED_EXCEEDS_COUNTS"));
        });
    }

    @Test
    public void testDuplicateMethodInCounts() {
        // Add a small catch
        final TestSmallCatch sc = act.withSmallCatch().month(Month.JANUARY)
                .counts(Count.of("methods/1", 2), Count.of("methods/1", 2), Count.of("methods/3", 2))
                .released(1);
        sc.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].entity", Matchers.equalTo("SmallCatch"));
            r.body("errors[0].property", Matchers.equalTo("counts"));
            r.body("errors[0].invalidValue", Matchers.hasSize(3));
            r.body("errors[0].message", Matchers.equalTo("SMALL_CATCH_COUNTS_METHOD_DUPLICATE_FOUND"));
        });
    }

    @Test
    public void testNullMethodValidation() {
        // Add a small catch
        final TestSmallCatch sc = act.withSmallCatch().month(Month.JANUARY)
                .counts(Count.of("methods/1", 2), Count.of(null, 2), Count.of("methods/3", 2))
                .released(1);

        sc.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].entity", Matchers.equalTo("SmallCatch"));
            r.body("errors[0].property", Matchers.equalTo("counts[1].method"));
            r.body("errors[0].invalidValue", Matchers.equalTo(null));
            r.body("errors[0].message", Matchers.equalTo("SMALL_CATCH_COUNTS_METHOD_REQUIRED"));
        });
    }

    @Test
    public void testNullCountValidation() {
        // Add a small catch
        final TestSmallCatch sc = act.withSmallCatch().month(Month.JANUARY)
                .counts(Count.of("methods/1", 2), Count.of("methods/2", null), Count.of("methods/3", 2))
                .released(1);
        sc.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].entity", Matchers.equalTo("SmallCatch"));
            r.body("errors[0].property", Matchers.equalTo("counts[1].count"));
            r.body("errors[0].invalidValue", Matchers.equalTo(null));
            r.body("errors[0].message", Matchers.equalTo("SMALL_CATCH_COUNTS_COUNT_REQUIRED"));
        });
    }
}
