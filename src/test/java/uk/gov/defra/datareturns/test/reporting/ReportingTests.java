package uk.gov.defra.datareturns.test.reporting;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.activities.Activity;
import uk.gov.defra.datareturns.data.model.activities.ActivityRepository;
import uk.gov.defra.datareturns.data.model.catches.Catch;
import uk.gov.defra.datareturns.data.model.catches.CatchRepository;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.reporting.catches.bycontact.CatchSummaryByContact;
import uk.gov.defra.datareturns.data.model.reporting.catches.bycontact.CatchSummaryByContactRepository;
import uk.gov.defra.datareturns.data.model.reporting.catches.summary.CatchSummary;
import uk.gov.defra.datareturns.data.model.reporting.catches.summary.CatchSummaryRepository;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.species.Species;
import uk.gov.defra.datareturns.data.model.species.SpeciesRepository;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionRepository;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionSource;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionStatus;
import uk.gov.defra.datareturns.test.activities.ActivityTests;
import uk.gov.defra.datareturns.test.catches.CatchTests;
import uk.gov.defra.datareturns.test.submissions.SubmissionTests;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@WithAdminUser
@Slf4j
public class ReportingTests {
    private static final String TEST_CONTACT_ID = "test-contact";
    @Inject
    private SubmissionRepository submissionRepository;
    @Inject
    private ActivityRepository activityRepository;
    @Inject
    private CatchRepository catchRepository;
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private SpeciesRepository speciesRepository;
    @Inject
    private MethodRepository methodRepository;
    @Inject
    private CatchSummaryByContactRepository catchSummaryByContactRepository;
    @Inject
    private CatchSummaryRepository catchSummaryRepository;
    private Submission submission;
    private Activity activity;
    private River river;
    private Method method;
    private Species species;

    @Before
    @Transactional
    public void setupTestData() {
        submissionRepository.deleteAll();

        river = riverRepository.getOne(1L);
        method = methodRepository.getOne(1L);
        species = speciesRepository.getOne(1L);

        submission = SubmissionTests.createValidSubmission(TEST_CONTACT_ID, Year.now().getValue(), SubmissionStatus.INCOMPLETE, SubmissionSource.WEB);
        submissionRepository.saveAndFlush(submission);

        activity = ActivityTests.createValidActivity(submission, river, 20, 20);
        activityRepository.saveAndFlush(activity);

        final Catch catch1 = CatchTests.createValidCatch(submission, activity, method, species, new BigDecimal("2.123456"), false);
        catchRepository.saveAndFlush(catch1);

        final Catch catch2 = CatchTests.createValidCatch(submission, activity, method, species, new BigDecimal("1.876544"), true);
        catchRepository.saveAndFlush(catch2);
    }

    @Test
    public void testCatchSummary() {
        final List<CatchSummary> summaryData = catchSummaryRepository.findBySeason((short) Year.now().getValue());
        Assertions.assertThat(summaryData).hasSize(1);
        final CatchSummary summary = summaryData.get(0);
        Assertions.assertThat(summary.getSeason()).isEqualTo((short) Year.now().getValue());
        Assertions.assertThat(summary.getMonth()).isEqualToIgnoringCase(MonthDay.now().getMonth().name());
        Assertions.assertThat(summary.getRiver()).isEqualTo(river.getName());
        Assertions.assertThat(summary.getSpecies()).isEqualTo(species.getName());
        Assertions.assertThat(summary.getCaught()).isEqualTo(2);
        Assertions.assertThat(summary.getReleased()).isEqualTo(1);
        Assertions.assertThat(summary.getCaughtTotalMass()).isEqualByComparingTo("4");
        Assertions.assertThat(summary.getCaughtAvgMass()).isEqualByComparingTo("2");
        Assertions.assertThat(summary.getCaughtMaxMass()).isEqualByComparingTo("2.123456");
        Assertions.assertThat(summary.getCaughtMinMass()).isEqualByComparingTo("1.876544");
        Assertions.assertThat(summary.getReleasedTotalMass()).isEqualByComparingTo("1.876544");
    }

    @Test
    public void testCatchSummaryByContact() {
        final List<CatchSummaryByContact> summaryData = catchSummaryByContactRepository.findBySeason((short) Year.now().getValue());
        Assertions.assertThat(summaryData).hasSize(1);
        final CatchSummaryByContact summary = summaryData.get(0);
        Assertions.assertThat(summary.getContactId()).isEqualTo(TEST_CONTACT_ID);
        Assertions.assertThat(summary.getSeason()).isEqualTo((short) Year.now().getValue());
        Assertions.assertThat(summary.getMonth()).isEqualToIgnoringCase(MonthDay.now().getMonth().name());
        Assertions.assertThat(summary.getRiver()).isEqualTo(river.getName());
        Assertions.assertThat(summary.getSpecies()).isEqualTo(species.getName());
        Assertions.assertThat(summary.getCaught()).isEqualTo(2);
        Assertions.assertThat(summary.getReleased()).isEqualTo(1);
        Assertions.assertThat(summary.getCaughtTotalMass()).isEqualByComparingTo("4");
        Assertions.assertThat(summary.getCaughtAvgMass()).isEqualByComparingTo("2");
        Assertions.assertThat(summary.getCaughtMaxMass()).isEqualByComparingTo("2.123456");
        Assertions.assertThat(summary.getCaughtMinMass()).isEqualByComparingTo("1.876544");
        Assertions.assertThat(summary.getReleasedTotalMass()).isEqualByComparingTo("1.876544");
    }
}
