package uk.gov.defra.datareturns.data.model.reporting.feeds;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
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
import uk.gov.defra.datareturns.data.model.reporting.filters.SeasonFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static uk.gov.defra.datareturns.util.CsvUtil.writeCsv;

/**
 * Controller to enable reporting functionality
 *
 * @author Sam Gardner-Dell
 */
@BasePathAwareController
@ConditionalOnWebApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reporting/feeds")
public class ReportingController implements ResourceProcessor<RepositoryLinksResource> {
    private final SubmissionFeedRepository submissionFeedRepository;
    private final ActivityFeedRepository activityFeedRepository;
    private final LargeCatchFeedRepository largeCatchFeedRepository;
    private final SmallCatchFeedRepository smallCatchFeedRepository;
    private final SmallCatchCountFeedRepository smallCatchCountFeedRepository;

    @GetMapping(value = "/submissions/{season}")
    @ApiOperation(value = "Retrieve reporting summary data by contact for the given season", produces = "text/csv")
    public void submissions(@PathVariable("season") final SeasonFilter season, final HttpServletResponse response) throws IOException {
        final Specification<SubmissionFeed> seasonSpec = (root, query, cb) -> season.predicate(cb, root.get(SubmissionFeed_.season));
        final Iterable<SubmissionFeed> submissions = submissionFeedRepository.findAll(seasonSpec);
        writeCsv(SubmissionFeed.class, submissions, response, "SubmissionFeed-" + season + ".csv");
    }

    @GetMapping(value = "/activities/{season}")
    @ApiOperation(value = "Retrieve full large catch data for the given filters", produces = "text/csv")
    public void activities(@PathVariable("season") final SeasonFilter season, final HttpServletResponse response) throws IOException {
        final Specification<ActivityFeed> seasonSpec = (root, query, cb) -> season.predicate(cb, root.get(ActivityFeed_.season));
        final List<ActivityFeed> entries = activityFeedRepository.findAll(seasonSpec);
        writeCsv(ActivityFeed.class, entries, response, "Activities-" + season + ".csv");
    }

    @GetMapping(value = "/large-catches/{season}")
    @ApiOperation(value = "Large catch reporting feed", produces = "text/csv")
    public void largeCatches(@PathVariable("season") final SeasonFilter season,
                             final HttpServletResponse response) throws IOException {
        final Specification<LargeCatchFeed> seasonSpec = (root, query, cb) -> season.predicate(cb, root.get(LargeCatchFeed_.season));
        final List<LargeCatchFeed> entries = largeCatchFeedRepository.findAll(seasonSpec);
        writeCsv(LargeCatchFeed.class, entries, response, "LargeCatchFeed-" + season + ".csv");
    }

    @GetMapping(value = "/small-catches/{season}")
    @ApiOperation(value = "Catch submission data feed", produces = "text/csv")
    public void smallCatches(@PathVariable("season") final SeasonFilter season,
                             final HttpServletResponse response) throws IOException {
        final Specification<SmallCatchFeed> seasonSpec = (root, query, cb) -> season.predicate(cb, root.get(SmallCatchFeed_.season));
        final List<SmallCatchFeed> entries = smallCatchFeedRepository.findAll(seasonSpec);
        writeCsv(SmallCatchFeed.class, entries, response, "SmallCatchFeed-" + season + ".csv");
    }

    @GetMapping(value = "/small-catch-counts/{season}")
    @ApiOperation(value = "Catch submission data feed", produces = "text/csv")
    public void smallCatchCounts(@PathVariable("season") final SeasonFilter season,
                                 final HttpServletResponse response) throws IOException {
        final Specification<SmallCatchCountFeed> seasonSpec = (root, query, cb) -> season.predicate(cb, root.get(SmallCatchCountFeed_.season));
        final List<SmallCatchCountFeed> entries = smallCatchCountFeedRepository.findAll(seasonSpec);
        writeCsv(SmallCatchCountFeed.class, entries, response, "SmallCatchCountFeed-" + season + ".csv");
    }

    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        final String base = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        resource.add(new Link(base + "reporting/feeds/submissions/{season}", "submissionsReportingFeed"));
        resource.add(new Link(base + "reporting/feeds/activities/{season}", "activitiesReportingFeed"));
        resource.add(new Link(base + "reporting/feeds/large-catches/{season}", "largeCatchesReporting"));
        resource.add(new Link(base + "reporting/feeds/small-catches/{season}", "smallCatchesReportingFeed"));
        resource.add(new Link(base + "reporting/feeds/small-catch-counts/{season}", "smallCatchCountsReportingFeed"));
        return resource;
    }
}
