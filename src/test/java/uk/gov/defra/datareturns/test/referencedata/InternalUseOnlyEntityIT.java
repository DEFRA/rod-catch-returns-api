package uk.gov.defra.datareturns.test.referencedata;

import io.restassured.response.ValidatableResponse;
import junit.framework.AssertionFailedError;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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
import uk.gov.defra.datareturns.testutils.client.TestActivity;
import uk.gov.defra.datareturns.testutils.client.TestCatch;
import uk.gov.defra.datareturns.testutils.client.TestSmallCatch;
import uk.gov.defra.datareturns.testutils.client.TestSubmission;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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

    private String internalMethodId;
    private String internalRiverId;

    @Before
    public void setup() {
        submissionRepository.deleteAll();

        final Method exampleMethod = new Method();
        exampleMethod.setInternal(true);
        final Method internalMethod = methodRepository.findAll(Example.of(exampleMethod)).stream().findFirst().orElseThrow(AssertionFailedError::new);
        internalMethodId = "methods/" + internalMethod.getId();
        final River exampleRiver = new River();
        exampleRiver.setInternal(true);
        final River internalRiver = riverRepository.findAll(Example.of(exampleRiver)).stream().findFirst().orElseThrow(AssertionFailedError::new);
        internalRiverId = "rivers/" + internalRiver.getId();
    }


    private void checkMethodOnCatch(final Consumer<ValidatableResponse> responseAssertions) {
        final TestSubmission sub = TestSubmission.of(DynamicsMockData.get(TestLicences.getLicence(1)).getContactId(), Year.now().getValue());
        sub.create();
        final TestActivity activity = sub.withActivity().river("rivers/1").daysFishedWithMandatoryRelease(1).daysFishedOther(1);
        activity.create();
        final TestCatch testCatch = activity.withCatch()
                .anyValidCatchDate()
                .method(internalMethodId)
                .species("species/1")
                .mass(CatchMass.MeasurementType.METRIC, BigDecimal.ONE)
                .released(false);
        testCatch.create(responseAssertions);
    }

    private void checkMethodOnSmallCatch(final Consumer<ValidatableResponse> responseAssertions) {
        final TestSubmission sub = TestSubmission.of(DynamicsMockData.get(TestLicences.getLicence(1)).getContactId(), Year.now().getValue());
        sub.create();
        final TestActivity activity = sub.withActivity().river("rivers/1").daysFishedWithMandatoryRelease(1).daysFishedOther(1);
        activity.create();
        final TestSmallCatch sc = activity.withSmallCatch()
                .month(Month.from(LocalDate.now()))
                .counts(TestSmallCatch.Count.of(internalMethodId, 5))
                .released(5);
        sc.create(responseAssertions);

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
        final TestSubmission sub = TestSubmission.of(DynamicsMockData.get(TestLicences.getLicence(1)).getContactId(), Year.now().getValue());
        sub.create();
        final TestActivity activity = sub.withActivity().river(internalRiverId).daysFishedWithMandatoryRelease(1).daysFishedOther(1);
        activity.create();
        Assertions.assertThat(activity.getUrl()).isNotNull();
    }

    @Test
    public void testInternalRiverOnActivityForEndUser() {
        final TestSubmission sub = TestSubmission.of(DynamicsMockData.get(TestLicences.getLicence(1)).getContactId(), Year.now().getValue());
        sub.create();
        final TestActivity activity = sub.withActivity().river(internalRiverId).daysFishedWithMandatoryRelease(1).daysFishedOther(1);
        activity.create((r) -> {
            r.statusCode(HttpStatus.BAD_REQUEST.value());
            r.body("errors", hasSize(1));
            r.body("errors[0].entity", equalTo("Activity"));
            r.body("errors[0].property", equalTo("river"));
            r.body("errors[0].message", equalTo("ACTIVITY_RIVER_FORBIDDEN"));
        });
    }
}
