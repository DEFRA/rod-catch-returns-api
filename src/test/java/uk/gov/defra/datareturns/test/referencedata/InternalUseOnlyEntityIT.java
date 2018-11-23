package uk.gov.defra.datareturns.test.referencedata;

import io.restassured.response.ValidatableResponse;
import junit.framework.AssertionFailedError;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionRepository;
import uk.gov.defra.datareturns.services.crm.DynamicsMockData;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.TestLicences;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.Collections;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.createEntity;
import static uk.gov.defra.datareturns.testutils.SubmissionITUtils.getActivityJson;
import static uk.gov.defra.datareturns.testutils.SubmissionITUtils.getCatchJson;
import static uk.gov.defra.datareturns.testutils.SubmissionITUtils.getSmallCatchJson;
import static uk.gov.defra.datareturns.testutils.SubmissionITUtils.getSubmissionJson;

/**
 * Integration tests for restricted entities
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@Slf4j
public class InternalUseOnlyEntityIT {
    @Inject
    private SubmissionRepository submissionRepository;
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private MethodRepository methodRepository;

    private Method internalMethod;
    private String internalMethodId;
    private River internalRiver;
    private String internalRiverId;
    private String submissionUrl;

    @Before
    public void setup() {
        submissionRepository.deleteAll();

        final String submissionJson = getSubmissionJson(DynamicsMockData.get(TestLicences.getLicence(1)).getContactId(), Year.now().getValue());
        submissionUrl = createEntity("/submissions", submissionJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });

        Method exampleMethod = new Method();
        exampleMethod.setInternal(true);
        internalMethod = methodRepository.findAll(Example.of(exampleMethod)).stream().findFirst().orElseThrow(AssertionFailedError::new);
        internalMethodId = "methods/" + internalMethod.getId();
        River exampleRiver = new River();
        exampleRiver.setInternal(true);
        internalRiver = riverRepository.findAll(Example.of(exampleRiver)).stream().findFirst().orElseThrow(AssertionFailedError::new);
        internalRiverId = "rivers/" + internalRiver.getId();
    }


    private void checkMethodOnCatch(final Consumer<ValidatableResponse> responseAssertions) {
        final String activityUrl = createValidTestActivity();
        final String catchJson = getCatchJson(submissionUrl, activityUrl, "species/1", internalMethodId,
                CatchMass.MeasurementType.METRIC, BigDecimal.ONE, false);
        createEntity("/catches", catchJson, responseAssertions);
    }

    private void checkMethodOnSmallCatch(final Consumer<ValidatableResponse> responseAssertions) {
        final String activityUrl = createValidTestActivity();
        final String smallCatchJson = getSmallCatchJson(submissionUrl, activityUrl, Month.MARCH, Collections.singletonMap(internalMethodId, 5), 5);
        createEntity("/smallCatches", smallCatchJson, responseAssertions);
    }

    @Test
    @WithAdminUser
    public void testInternalMethodOnCatchForAdmin() {
        checkMethodOnCatch((r) -> r.statusCode(HttpStatus.CREATED.value()));
    }

    @Test
    public void testInternalMethodOnCatchForEndUser() {
        checkMethodOnCatch((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", hasSize(1));
            r.body("errors[0].entity", equalTo("Catch"));
            r.body("errors[0].property", equalTo("method"));
            r.body("errors[0].message", equalTo("CATCH_METHOD_FORBIDDEN"));
        });
    }

    @Test
    @WithAdminUser
    public void testInternalMethodOnSmallCatchForAdmin() {
        checkMethodOnSmallCatch((r) -> r.statusCode(HttpStatus.CREATED.value()));
    }

    @Test
    public void testInternalMethodOnSmallCatchForEndUser() {
        checkMethodOnSmallCatch((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", hasSize(1));
            r.body("errors[0].entity", equalTo("SmallCatch"));
            r.body("errors[0].property", equalTo("counts[0].method"));
            r.body("errors[0].message", equalTo("SMALL_CATCH_COUNTS_METHOD_FORBIDDEN"));
        });
    }

    @Test
    @WithAdminUser
    public void testInternalRiverOnActivityForAdmin() {
        final String activityJson = getActivityJson(submissionUrl, internalRiverId, 5, 5);
        createEntity("/activities", activityJson, (r) -> r.statusCode(HttpStatus.CREATED.value()));
    }

    @Test
    public void testInternalRiverOnActivityForEndUser() {
        final String activityJson = getActivityJson(submissionUrl, internalRiverId, 5, 5);
        createEntity("/activities", activityJson, (r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", hasSize(1));
            r.body("errors[0].entity", equalTo("Activity"));
            r.body("errors[0].property", equalTo("river"));
            r.body("errors[0].message", equalTo("ACTIVITY_RIVER_FORBIDDEN"));
        });
    }

    private String createValidTestActivity() {
        final String activityJson = getActivityJson(submissionUrl, "rivers/1", 5, 5);
        return createEntity("/activities", activityJson, (r) -> {
            r.statusCode(HttpStatus.CREATED.value());
            r.body("errors", Matchers.nullValue());
        });
    }
}
