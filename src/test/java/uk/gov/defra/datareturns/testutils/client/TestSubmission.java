package uk.gov.defra.datareturns.testutils.client;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TestSubmission extends AbstractTestEntity {
    static final String CONTACT_ID = "contactId";
    static final String SEASON = "season";
    static final String SOURCE = "source";
    static final String STATUS = "status";
    static final String REPORTING_EXCLUSION = "reportingExclude";

    @Getter
    private final List<TestActivity> activities = new ArrayList<>();

    public TestActivity withActivity() {
        final TestActivity ab = new TestActivity(this);
        activities.add(ab);
        return ab;
    }

    public static TestSubmission of(final String contactId, final int season) {
        final TestSubmission sub = new TestSubmission();
        sub.modify(CONTACT_ID, contactId);
        sub.modify(SEASON, season);
        sub.status("INCOMPLETE");
        sub.source("WEB");
        return sub;
    }


    public TestSubmission source(final String source) {
        modify(SOURCE, source);
        return this;
    }

    public TestSubmission status(final String status) {
        modify(STATUS, status);
        return this;
    }

    public TestSubmission reportingExclude(final Boolean exclude) {
        modify(REPORTING_EXCLUSION, exclude);
        return this;
    }

    @Override
    String getResourcePath() {
        return "/submissions";
    }
}
