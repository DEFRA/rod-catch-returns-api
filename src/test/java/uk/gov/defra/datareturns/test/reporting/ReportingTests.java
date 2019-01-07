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
import uk.gov.defra.datareturns.data.model.catchments.Catchment;
import uk.gov.defra.datareturns.data.model.method.Method;
import uk.gov.defra.datareturns.data.model.method.MethodRepository;
import uk.gov.defra.datareturns.data.model.regions.Region;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.LargeCatchFeed;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.LargeCatchFeedRepository;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.LargeCatchFeed_;
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
import java.time.Month;
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
    private final Month month = MonthDay.now().getMonth();
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
    private LargeCatchFeedRepository largeCatchFeedRepository;
    private Submission submission;
    private Activity activity;
    private Region region;
    private Catchment catchment;
    private River river;
    private Method method;
    private Species species;
    private Catch catch1;
    private Catch catch2;

    @Before
    @Transactional
    public void setupTestData() {
        submissionRepository.deleteAll();

        river = riverRepository.getOne(1L);
        catchment = river.getCatchment();
        region = catchment.getRegion();
        method = methodRepository.getOne(1L);
        species = speciesRepository.getOne(1L);

        submission = SubmissionTests.createValidSubmission(TEST_CONTACT_ID, Year.now().getValue(), SubmissionStatus.INCOMPLETE, SubmissionSource.WEB);
        submissionRepository.saveAndFlush(submission);
        activity = ActivityTests.createValidActivity(submission, river, 20, 20);
        activityRepository.saveAndFlush(activity);

        catch1 = CatchTests.createValidCatch(submission, activity, method, species, new BigDecimal("2.123456"), false);
        catchRepository.saveAndFlush(catch1);

        catch2 = CatchTests.createValidCatch(submission, activity, method, species, new BigDecimal("1.876544"), true);
        catchRepository.saveAndFlush(catch2);
    }

    private List<LargeCatchFeed> getLargeCatchReportData() {
        return largeCatchFeedRepository.findAll((root, query, cb) -> cb.equal(root.get(LargeCatchFeed_.season), Year.now().getValue()));
    }


    @Test
    public void testLargeCatchReporting() {
        final List<LargeCatchFeed> catchData = getLargeCatchReportData();
        LargeCatchFeed data1 = catchData.get(0);
        Assertions.assertThat(data1.getId()).isEqualTo(catch1.getId());
        Assertions.assertThat(data1.getSeason()).isEqualTo((short) Year.now().getValue());
        Assertions.assertThat(data1.getActivityId()).isEqualTo(activity.getId());
        Assertions.assertThat(data1.getDateCaught()).isEqualToIgnoringHours(catch1.getDateCaught());
        Assertions.assertThat(data1.getMethodId()).isEqualTo(catch1.getMethod().getId());
        Assertions.assertThat(data1.getSpeciesId()).isEqualTo(catch1.getSpecies().getId());
        Assertions.assertThat(data1.getMass()).isEqualTo(catch1.getMass().getKg());
        Assertions.assertThat(data1.getReleased()).isEqualTo(catch1.isReleased());

        LargeCatchFeed data2 = catchData.get(1);
        Assertions.assertThat(data2.getId()).isEqualTo(catch2.getId());
        Assertions.assertThat(data2.getSeason()).isEqualTo((short) Year.now().getValue());
        Assertions.assertThat(data2.getActivityId()).isEqualTo(activity.getId());
        Assertions.assertThat(data2.getDateCaught()).isEqualToIgnoringHours(catch2.getDateCaught());
        Assertions.assertThat(data2.getMethodId()).isEqualTo(catch2.getMethod().getId());
        Assertions.assertThat(data2.getSpeciesId()).isEqualTo(catch2.getSpecies().getId());
        Assertions.assertThat(data2.getMass()).isEqualTo(catch2.getMass().getKg());
        Assertions.assertThat(data2.getReleased()).isEqualTo(catch2.isReleased());
    }

    @Test
    @Transactional
    public void testSubmissionExclusions() {
        submission.setReportingExclude(true);
        submissionRepository.saveAndFlush(submission);
        final List<LargeCatchFeed> catchData = getLargeCatchReportData();
        Assertions.assertThat(catchData).hasSize(0);
    }

    @Test
    @Transactional
    public void testCatchExclusions() {
        catch1.setReportingExclude(true);
        catchRepository.saveAndFlush(catch1);
        final List<LargeCatchFeed> catchData = getLargeCatchReportData();
        Assertions.assertThat(catchData).hasSize(1);
    }
}
