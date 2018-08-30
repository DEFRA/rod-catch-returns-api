package uk.gov.defra.datareturns.test.catches;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.species.SpeciesRepository;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.test.activities.ActivityTests;
import uk.gov.defra.datareturns.test.submissions.SubmissionTests;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;
import uk.gov.defra.datareturns.testutils.SubmissionTestUtils;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@WebIntegrationTest
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

    @Test
    public void testValidCatch() {
        final Catch cat = createValidCatch();
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).isEmpty();
    }

    @Test
    public void testCatchWithoutDateFails() {
        final Catch cat = createValidCatch();
        cat.setDateCaught(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_DATE_REQUIRED"));
    }

    @Test
    public void testCatchWithDateMismatchToSubmissionFails() {
        final Catch cat = createValidCatch();
        cat.getDateCaught().setYear(cat.getSubmission().getSeasonEnding() - 1);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_YEAR_MISMATCH"));
    }

    @Test
    public void testCatchWithoutRiverFails() {
        final Catch cat = createValidCatch();
        cat.setRiver(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_RIVER_REQUIRED"));
    }

    @Test
    public void testCatchWithoutRiverDefinedInActivityFails() {
        final Catch cat = createValidCatch();
        cat.getSubmission().setSubmissionActivities(new HashSet<>());
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1)
                .haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_RIVER_NOT_DEFINED_IN_ACTIVITIES"));
    }

    @Test
    public void testCatchWithoutSpeciesFails() {
        final Catch cat = createValidCatch();
        cat.setSpecies(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_SPECIES_REQUIRED"));
    }

    @Test
    public void testCatchWithMaxWeightExceededFails() {
        final Catch cat = createValidCatch();
        cat.getMass().setKg(BigDecimal.valueOf(50.1));
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_MASS_MAX_EXCEEDED"));
    }

    @Test
    public void testCatchBelowMinWeightFails() {
        final Catch cat = createValidCatch();
        cat.getMass().set(CatchMass.MeasurementType.Metric, BigDecimal.valueOf(0.453591));
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_MASS_BELOW_MINIMUM"));
    }

    @Test
    public void testCatchWithoutMethodFails() {
        final Catch cat = createValidCatch();
        cat.setMethod(null);
        final Set<ConstraintViolation<Catch>> violations = validator.validate(cat);
        Assertions.assertThat(violations).hasSize(1).haveAtLeastOne(SubmissionTestUtils.violationMessageMatching("CATCH_METHOD_REQUIRED"));
    }

    private Catch createValidCatch() {
        final River targetRiver = riverRepository.getOne(RandomUtils.nextLong(1, 100));
        return createValidCatch(SubmissionTests.createValidSubmission(), targetRiver, (short) 100);
    }


    private Catch createValidCatch(final Submission submission, final River river, final int days) {
        final Activity activity = ActivityTests.createValidActivity(submission, river, days);
        submission.setSubmissionActivities(Collections.singleton(activity));

        final Catch cat = new Catch();
        cat.setSubmission(submission);
        cat.setRiver(river);
        cat.setDateCaught(new Date());
        cat.setSpecies(speciesRepository.getOne(RandomUtils.nextLong(1, speciesRepository.count())));
        cat.setMethod(methodRepository.getOne(RandomUtils.nextLong(1, methodRepository.count())));
        cat.getMass().set(CatchMass.MeasurementType.Metric, BigDecimal.valueOf(0.98765432112));
        cat.setReleased(false);
        return cat;
    }
}
