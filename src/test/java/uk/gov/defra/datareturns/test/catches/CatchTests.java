package uk.gov.defra.datareturns.test.catches;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.species.Species;
import uk.gov.defra.datareturns.data.model.species.SpeciesRepository;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.test.activities.ActivityTests;
import uk.gov.defra.datareturns.test.submissions.SubmissionTests;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.violationMessageMatching;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@WithAdminUser
@Slf4j
public class CatchTests {
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private MethodRepository methodRepository;
    @Inject
    private SpeciesRepository speciesRepository;
    @Inject
    private Validator validator;

    public static Catch createValidCatch(final Submission submission, final Activity activity, final Method method, final Species species,
                                         final BigDecimal kg, final boolean released) {
        final Catch cat = new Catch();
        cat.setSubmission(submission);
        cat.setActivity(activity);
        cat.setDateCaught(new Date());
        cat.setSpecies(species);
        cat.setMethod(method);
        cat.getMass().set(CatchMass.MeasurementType.METRIC, kg);
        cat.setReleased(released);
        return cat;
    }

    @Test
    public void testValidCatch() {
        final Catch cat = createValidCatch();
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void testCatchWithoutSubmissionFails() {
        final Catch cat = createValidCatch();
        cat.setSubmission(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_SUBMISSION_REQUIRED"));
    }

    @Test
    public void testCatchWithoutDateFails() {
        final Catch cat = createValidCatch();
        cat.setDateCaught(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_DATE_REQUIRED"));
    }

    @Test
    public void testCatchWithFutureDateFails() {
        final Catch cat = createValidCatch();
        Date tomorrow = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
        cat.setDateCaught(tomorrow);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_DATE_IN_FUTURE"));
    }

    @Test
    public void testCatchWithDateMismatchToSubmissionFails() {
        final Catch cat = createValidCatch();
        cat.setDateCaught(DateUtils.addYears(cat.getDateCaught(), -1));
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_YEAR_MISMATCH"));
    }

    @Test
    public void testCatchWithoutActivityFails() {
        final Catch cat = createValidCatch();
        cat.setActivity(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_ACTIVITY_REQUIRED"));
    }

    @Test
    public void testCatchWithoutSpeciesFails() {
        final Catch cat = createValidCatch();
        cat.setSpecies(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_SPECIES_REQUIRED"));
    }

    @Test
    public void testCatchWithoutMassFails() {
        final Catch cat = createValidCatch();
        cat.setMass(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_MASS_REQUIRED"));
    }

    @Test
    public void testCatchWithoutMassTypeFails() {
        final Catch cat = createValidCatch();
        cat.getMass().setType(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_MASS_TYPE_REQUIRED"));
    }

    @Test
    public void testCatchWithoutMassOzValueFails() {
        final CatchMass catchMass = new CatchMass();
        catchMass.set(CatchMass.MeasurementType.IMPERIAL, null);

        final Catch cat = createValidCatch();
        cat.setMass(catchMass);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_MASS_OZ_REQUIRED"));
    }

    @Test
    public void testCatchWithoutMassKgValueFails() {
        final CatchMass catchMass = new CatchMass();
        catchMass.set(CatchMass.MeasurementType.METRIC, null);

        final Catch cat = createValidCatch();
        cat.setMass(catchMass);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_MASS_KG_REQUIRED"));
    }

    @Test
    public void testCatchWithMaxWeightExceededFails() {
        final Catch cat = createValidCatch();
        cat.getMass().setKg(BigDecimal.valueOf(50.1));
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_MASS_MAX_EXCEEDED"));
    }

    @Test
    public void testCatchBelowMinWeightFails() {
        final Catch cat = createValidCatch();
        cat.getMass().set(CatchMass.MeasurementType.METRIC, BigDecimal.valueOf(-0.0001));
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_MASS_BELOW_MINIMUM"));
    }

    @Test
    public void testCatchWithoutMethodFails() {
        final Catch cat = createValidCatch();
        cat.setMethod(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).haveExactly(1, violationMessageMatching("CATCH_METHOD_REQUIRED"));
    }

    private Catch createValidCatch() {
        final River targetRiver = riverRepository.getOne(RandomUtils.nextLong(1, 100));
        return createValidCatch(SubmissionTests.createValidSubmission(), targetRiver);
    }

    public Catch createValidCatch(final Submission submission, final River river) {
        final Activity activity = ActivityTests.createValidActivity(submission, river, 100, 100);
        submission.setActivities(Collections.singletonList(activity));
        final Species species = speciesRepository.getOne(RandomUtils.nextLong(1, speciesRepository.count()));
        final Method method = methodRepository.getOne(RandomUtils.nextLong(1, methodRepository.count()));
        return createValidCatch(submission, activity, method, species, BigDecimal.valueOf(0.98765432112), false);
    }
}
