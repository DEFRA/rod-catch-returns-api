package uk.gov.defra.datareturns.testutils.client;

import lombok.Getter;
import org.springframework.data.util.Pair;

import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class TestSmallCatch extends AbstractTestEntity {
    private final TestActivity activity;
    public static final String SUBMISSION = "submission";
    public static final String ACTIVITY = "activity";
    public static final String MONTH = "month";
    public static final String COUNTS = "counts";
    public static final String RELEASED = "released";

    TestSmallCatch(final TestActivity activity) {
        modify(SUBMISSION, () -> activity.getSubmission().getUrl());
        modify(ACTIVITY, activity::getUrl);
        this.activity = activity;
    }

    public final TestSmallCatch month(final Month month) {
        modify(MONTH, month.name());
        return this;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public final TestSmallCatch counts(final Pair<String, Integer>... counts) {
        final List<Map<String, Object>> countObj = Arrays.stream(counts).map(c -> {
            Map<String, Object> countEntry = new HashMap<>();
            countEntry.put("method", c.getFirst());
            countEntry.put("count", c.getSecond());
            return countEntry;
        }).collect(Collectors.toList());
        modify(COUNTS, () -> countObj);
        return this;
    }

    public final TestSmallCatch released(final int released) {
        modify(RELEASED, released);
        return this;
    }

    @Override
    String getResourcePath() {
        return "/smallCatches";
    }
}
