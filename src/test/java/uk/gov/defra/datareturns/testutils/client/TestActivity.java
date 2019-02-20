package uk.gov.defra.datareturns.testutils.client;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestActivity extends AbstractTestEntity {
    public static final String RIVER = "river";
    public static final String DAYS_FISHED_WITH_MANDATORY_RELEASE = "daysFishedWithMandatoryRelease";
    public static final String DAYS_FISHED_OTHER = "daysFishedOther";
    public static final String SUBMISSION = "submission";

    private final TestSubmission submission;
    private final List<TestCatch> catches = new ArrayList<>();
    private final List<TestSmallCatch> smallCatches = new ArrayList<>();

    TestActivity(final TestSubmission submission) {
        this.submission = submission;
        modify(SUBMISSION, submission::getUrl);
    }

    public TestCatch withCatch() {
        final TestCatch c = new TestCatch(this);
        catches.add(c);
        return c;
    }


    public TestSmallCatch withSmallCatch() {
        final TestSmallCatch c = new TestSmallCatch(this);
        smallCatches.add(c);
        return c;
    }

    public TestActivity river(final String riverId) {
        modify(RIVER, riverId);
        return this;
    }

    public TestActivity daysFishedWithMandatoryRelease(final int daysFishedWithMandatoryRelease) {
        modify(DAYS_FISHED_WITH_MANDATORY_RELEASE, daysFishedWithMandatoryRelease);
        return this;
    }

    public TestActivity daysFishedOther(final int daysFishedOther) {
        modify(DAYS_FISHED_OTHER, daysFishedOther);
        return this;
    }

    @Override
    String getResourcePath() {
        return "/activities";
    }
}
