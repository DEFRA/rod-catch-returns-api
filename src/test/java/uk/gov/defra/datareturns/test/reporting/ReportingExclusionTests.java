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
import uk.gov.defra.datareturns.data.model.reporting.feeds.activities.ActivityFeed;
import uk.gov.defra.datareturns.data.model.reporting.feeds.activities.ActivityFeedRepository;
import uk.gov.defra.datareturns.data.model.reporting.feeds.activities.ActivityFeed_;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.LargeCatchFeed;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.LargeCatchFeedRepository;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.LargeCatchFeed_;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.SmallCatchCountFeed;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.SmallCatchCountFeedRepository;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.SmallCatchCountFeed_;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.SmallCatchFeed;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.SmallCatchFeedRepository;
import uk.gov.defra.datareturns.data.model.reporting.feeds.catches.SmallCatchFeed_;
import uk.gov.defra.datareturns.data.model.reporting.feeds.submissions.SubmissionFeed;
import uk.gov.defra.datareturns.data.model.reporting.feeds.submissions.SubmissionFeedRepository;
import uk.gov.defra.datareturns.data.model.reporting.feeds.submissions.SubmissionFeed_;
import uk.gov.defra.datareturns.data.model.rivers.River;
import uk.gov.defra.datareturns.data.model.rivers.RiverRepository;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatch;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchCount;
import uk.gov.defra.datareturns.data.model.smallcatches.SmallCatchRepository;
import uk.gov.defra.datareturns.data.model.species.Species;
import uk.gov.defra.datareturns.data.model.species.SpeciesRepository;
import uk.gov.defra.datareturns.data.model.submissions.Submission;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionRepository;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionSource;
import uk.gov.defra.datareturns.data.model.submissions.SubmissionStatus;
import uk.gov.defra.datareturns.test.activities.ActivityTests;
import uk.gov.defra.datareturns.test.catches.CatchTests;
import uk.gov.defra.datareturns.test.smallcatches.SmallCatchCountTests;
import uk.gov.defra.datareturns.test.smallcatches.SmallCatchTests;
import uk.gov.defra.datareturns.test.submissions.SubmissionTests;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Integration tests catch object property validation
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@WithAdminUser
@Slf4j
public class ReportingExclusionTests {
    private static final String TEST_CONTACT_ID = "test-contact";
    @Inject
    private SubmissionRepository submissionRepository;
    @Inject
    private ActivityRepository activityRepository;
    @Inject
    private CatchRepository catchRepository;
    @Inject
    private SmallCatchRepository smallCatchRepository;
    @Inject
    private RiverRepository riverRepository;
    @Inject
    private SpeciesRepository speciesRepository;
    @Inject
    private MethodRepository methodRepository;

    @Inject
    private SubmissionFeedRepository submissionFeedRepository;
    @Inject
    private ActivityFeedRepository activityFeedRepository;
    @Inject
    private LargeCatchFeedRepository largeCatchFeedRepository;
    @Inject
    private SmallCatchFeedRepository smallCatchFeedRepository;
    @Inject
    private SmallCatchCountFeedRepository smallCatchCountFeedRepository;

    private Submission submission;
    private Activity activity;
    private Catch largeCatch;
    private SmallCatch smallCatch;

    @Before
    @Transactional
    public void setupTestData() {
        submissionRepository.deleteAll();

        final River river = riverRepository.getOne(1L);
        final Method method = methodRepository.getOne(1L);
        final Species species = speciesRepository.getOne(1L);

        submission = submissionRepository.saveAndFlush(SubmissionTests.createValidSubmission(TEST_CONTACT_ID, Year.now().getValue(),
                SubmissionStatus.INCOMPLETE, SubmissionSource.WEB));
        activity = activityRepository.saveAndFlush(ActivityTests.createValidActivity(submission, river, 20, 20));
        largeCatch = catchRepository
                .saveAndFlush(CatchTests.createValidCatch(submission, activity, method, species, new BigDecimal("2.123456"), false));

        final List<SmallCatchCount> counts = new ArrayList<>(Collections.singletonList(SmallCatchCountTests.createValidSmallCatchCount(method, 6)));
        smallCatch = smallCatchRepository.saveAndFlush(SmallCatchTests.createSmallCatch(submission, activity, counts, 3));
    }

    @Test
    public void testAllDataReportable() {
        Assertions.assertThat(getSubmissionData()).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", submission.getId())
                .hasFieldOrPropertyWithValue("contactId", submission.getContactId())
                .hasFieldOrPropertyWithValue("season", submission.getSeason())
                .hasFieldOrPropertyWithValue("status", submission.getStatus().name())
                .hasFieldOrPropertyWithValue("source", submission.getSource().name())
                .hasFieldOrPropertyWithValue("created", Timestamp.from(submission.getCreated().toInstant()))
                .hasFieldOrPropertyWithValue("lastModified", Timestamp.from(submission.getLastModified().toInstant()));

        Assertions.assertThat(getActivityData()).hasSize(1).first()
                .hasFieldOrPropertyWithValue("id", activity.getId())
                .hasFieldOrPropertyWithValue("season", submission.getSeason())
                .hasFieldOrPropertyWithValue("submissionId", submission.getId())
                .hasFieldOrPropertyWithValue("riverId", activity.getRiver().getId())
                .hasFieldOrPropertyWithValue("daysFishedWithMandatoryRelease", activity.getDaysFishedWithMandatoryRelease())
                .hasFieldOrPropertyWithValue("daysFishedOther", activity.getDaysFishedOther());

        Assertions.assertThat(getLargeCatchReportData()).hasSize(1).first()
                .hasFieldOrPropertyWithValue("id", largeCatch.getId())
                .hasFieldOrPropertyWithValue("season", submission.getSeason())
                .hasFieldOrPropertyWithValue("activityId", largeCatch.getActivity().getId())
                .hasFieldOrPropertyWithValue("dateCaught", Timestamp.from(largeCatch.getDateCaught().toInstant().truncatedTo(ChronoUnit.DAYS)))
                .hasFieldOrPropertyWithValue("methodId", largeCatch.getMethod().getId())
                .hasFieldOrPropertyWithValue("speciesId", largeCatch.getSpecies().getId())
                .hasFieldOrPropertyWithValue("mass", largeCatch.getMass().getKg())
                .hasFieldOrPropertyWithValue("released", largeCatch.isReleased());

        Assertions.assertThat(getSmallCatchReportData()).hasSize(1).first()
                .hasFieldOrPropertyWithValue("id", smallCatch.getId())
                .hasFieldOrPropertyWithValue("season", submission.getSeason())
                .hasFieldOrPropertyWithValue("activityId", smallCatch.getActivity().getId())
                .hasFieldOrPropertyWithValue("month", (short) smallCatch.getMonth().getValue())
                .hasFieldOrPropertyWithValue("speciesId", 2)
                .hasFieldOrPropertyWithValue("released", smallCatch.getReleased());

        Assertions.assertThat(getSmallCatchCountReportData()).hasSize(1).first()
                .hasFieldOrPropertyWithValue("id", smallCatch.getId() + "_" + smallCatch.getCounts().get(0).getMethod().getId())
                .hasFieldOrPropertyWithValue("season", submission.getSeason())
                .hasFieldOrPropertyWithValue("smallCatchId", smallCatch.getId())
                .hasFieldOrPropertyWithValue("methodId", smallCatch.getCounts().get(0).getMethod().getId())
                .hasFieldOrPropertyWithValue("count", smallCatch.getCounts().get(0).getCount());
    }

    @Test
    @Transactional
    public void testSubmissionExclusions() {
        submission.setReportingExclude(true);
        submissionRepository.saveAndFlush(submission);

        Assertions.assertThat(getSubmissionData()).hasSize(0);
        Assertions.assertThat(getActivityData()).hasSize(0);
        Assertions.assertThat(getLargeCatchReportData()).hasSize(0);
        Assertions.assertThat(getSmallCatchReportData()).hasSize(0);
        Assertions.assertThat(getSmallCatchCountReportData()).hasSize(0);
    }

    @Test
    @Transactional
    public void testCatchExclusions() {
        largeCatch.setReportingExclude(true);
        catchRepository.saveAndFlush(largeCatch);

        Assertions.assertThat(getSubmissionData()).hasSize(1);
        Assertions.assertThat(getActivityData()).hasSize(1);
        Assertions.assertThat(getLargeCatchReportData()).hasSize(0);
        Assertions.assertThat(getSmallCatchReportData()).hasSize(1);
        Assertions.assertThat(getSmallCatchCountReportData()).hasSize(1);
    }

    @Test
    @Transactional
    public void testSmallCatchExclusions() {
        smallCatch.setReportingExclude(true);
        smallCatchRepository.saveAndFlush(smallCatch);

        Assertions.assertThat(getSubmissionData()).hasSize(1);
        Assertions.assertThat(getActivityData()).hasSize(1);
        Assertions.assertThat(getLargeCatchReportData()).hasSize(1);
        Assertions.assertThat(getSmallCatchReportData()).hasSize(0);
        Assertions.assertThat(getSmallCatchCountReportData()).hasSize(0);
    }

    private List<SubmissionFeed> getSubmissionData() {
        return submissionFeedRepository.findAll((root, query, cb) -> cb.equal(root.get(SubmissionFeed_.season), Year.now().getValue()));
    }

    private List<ActivityFeed> getActivityData() {
        return activityFeedRepository.findAll((root, query, cb) -> cb.equal(root.get(ActivityFeed_.season), Year.now().getValue()));
    }

    private List<LargeCatchFeed> getLargeCatchReportData() {
        return largeCatchFeedRepository.findAll((root, query, cb) -> cb.equal(root.get(LargeCatchFeed_.season), Year.now().getValue()));
    }

    private List<SmallCatchFeed> getSmallCatchReportData() {
        return smallCatchFeedRepository.findAll((root, query, cb) -> cb.equal(root.get(SmallCatchFeed_.season), Year.now().getValue()));
    }

    private List<SmallCatchCountFeed> getSmallCatchCountReportData() {
        return smallCatchCountFeedRepository.findAll((root, query, cb) -> cb.equal(root.get(SmallCatchCountFeed_.season), Year.now().getValue()));
    }
}
