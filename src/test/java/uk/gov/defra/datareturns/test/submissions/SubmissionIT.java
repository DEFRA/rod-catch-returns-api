package uk.gov.defra.datareturns.test.submissions;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;
import uk.gov.defra.datareturns.testcommons.restassured.RestAssuredRule;

import javax.inject.Inject;

import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.fromJson;
import static uk.gov.defra.datareturns.testutils.SubmissionTestUtils.runSubmissionTest;

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
    public void testSimpleSubmission() {
        runSubmissionTest(fromJson("/data/valid/submission.json"), (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });
    }

    @Test
    public void testOneShotSubmission() {
        runSubmissionTest(fromJson("/data/valid/one-shot-submission.json"), (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });
    }
    @Test
    public void testEmptySubmissionFails() {
        runSubmissionTest(fromJson("/data/invalid/submission_empty.json"), (r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
        });
    }
}
