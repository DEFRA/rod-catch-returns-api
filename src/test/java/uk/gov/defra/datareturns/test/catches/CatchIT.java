package uk.gov.defra.datareturns.test.catches;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionRepository;
import uk.gov.defra.datareturns.services.crm.DynamicsMockData;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.TestLicences;
import uk.gov.defra.datareturns.testutils.client.TestActivity;
import uk.gov.defra.datareturns.testutils.client.TestCatch;
import uk.gov.defra.datareturns.testutils.client.TestSubmission;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Year;

/**
 * Integration tests for large catch api interactions
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@Slf4j
public class CatchIT {
    @Inject
    private SubmissionRepository submissionRepository;

    private TestActivity act;
    private TestSubmission sub;

    @Before
    public void setup() {
        submissionRepository.deleteAll();

        final String contactId = DynamicsMockData.get(TestLicences.getLicence(1)).getContactId();
        final int season = Year.now().getValue() - 1;
        sub = TestSubmission.of(contactId, season);
        act = sub.withActivity().river("rivers/3").daysFishedWithMandatoryRelease(20).daysFishedOther(5);
        sub.create();
        act.create();
    }

    @Test
    public void testCatchWithNullReleased() {
        final TestCatch cat = act.withCatch()
                .anyValidCatchDate()
                .method("methods/1")
                .species("species/1")
                .mass(CatchMass.MeasurementType.METRIC, BigDecimal.ONE)
                .released(null);

        cat.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", Matchers.hasSize(1));
            r.body("errors[0].entity", Matchers.equalTo("Catch"));
            r.body("errors[0].property", Matchers.equalTo("released"));
            r.body("errors[0].invalidValue", Matchers.equalTo(null));
            r.body("errors[0].message", Matchers.equalTo("CATCH_RELEASED_REQUIRED"));
        });
        sub.delete();
    }
}
