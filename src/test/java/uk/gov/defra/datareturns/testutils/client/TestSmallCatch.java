package uk.gov.defra.datareturns.testutils.client;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class TestSmallCatch extends AbstractTestEntity {
    private final TestActivity activity;
    public static final String ACTIVITY = "activity";
    public static final String MONTH = "month";
    public static final String COUNTS = "counts";
    public static final String RELEASED = "released";
    public static final String REPORTING_EXCLUSION = "reportingExclude";

    TestSmallCatch(final TestActivity activity) {
        modify(ACTIVITY, activity::getUrl);
        this.activity = activity;
    }

    public final TestSmallCatch month(final Month month) {
        return month(month.name());
    }


    public final TestSmallCatch month(final String month) {
        modify(MONTH, month);
        return this;
    }

    public final TestSmallCatch counts(final Count... counts) {
        final List<Map<String, Object>> countObj = Arrays.stream(counts).map(entry -> {
            Map<String, Object> countEntry = new HashMap<>();
            countEntry.put("method", entry.getMethod());
            countEntry.put("count", entry.getCount());
            return countEntry;
        }).collect(Collectors.toList());
        modify(COUNTS, () -> countObj);
        return this;
    }


    public final TestSmallCatch released(final int released) {
        modify(RELEASED, released);
        return this;
    }

    public final TestSmallCatch reportingExclude(final Boolean exclude) {
        modify(REPORTING_EXCLUSION, exclude);
        return this;
    }

    @Override
    String getResourcePath() {
        return "/smallCatches";
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    public static class Count {
        private final String method;
        private final Integer count;
    }
}
