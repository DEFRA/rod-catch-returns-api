package uk.gov.defra.datareturns.testutils.client;

import lombok.Getter;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
public class TestCatch extends AbstractTestEntity {
    private final TestActivity activity;
    public static final String ACTIVITY = "activity";
    public static final String DATE_CAUGHT = "dateCaught";
    public static final String SPECIES = "species";
    public static final String METHOD = "method";
    public static final String MASS = "mass";
    public static final String RELEASED = "released";
    public static final String REPORTING_EXCLUSION = "reportingExclude";
    public static final String NO_DATE_RECORDED = "noDateRecorded";
    public static final String ONLY_MONTH_RECORDED = "onlyMonthRecorded";

    TestCatch(final TestActivity activity) {
        modify(ACTIVITY, activity::getUrl);
        this.activity = activity;
    }

    public TestCatch anyValidCatchDate() {
        final Integer season = activity.getSubmission().getIntegerValue(TestSubmission.SEASON);
        dateCaught(LocalDate.now().withYear(season).minusDays(1));
        return this;
    }

    public final TestCatch dateCaught(final LocalDate date) {
        modify(DATE_CAUGHT, date);
        return this;
    }

    public final TestCatch species(final String species) {
        modify(SPECIES, species);
        return this;
    }

    public final TestCatch method(final String method) {
        modify(METHOD, method);
        return this;
    }

    public final TestCatch reportingExclude(final Boolean exclude) {
        modify(REPORTING_EXCLUSION, exclude);
        return this;
    }

    public final TestCatch noDateRecorded(final Boolean noDateRecorded) {
        modify(NO_DATE_RECORDED, noDateRecorded);
        return this;
    }

    public final TestCatch onlyMonthRecorded(final Boolean onlyMonthRecorded) {
        modify(ONLY_MONTH_RECORDED, onlyMonthRecorded);
        return this;
    }

    public final TestCatch mass(final CatchMass.MeasurementType massType, final BigDecimal mass) {
        final Map<String, Object> massObj = new HashMap<>();
        massObj.put("type", massType.name());
        if (CatchMass.MeasurementType.METRIC.equals(massType)) {
            massObj.put("kg", mass);
        } else {
            massObj.put("oz", mass);
        }
        modify(MASS, () -> massObj);
        return this;
    }

    public final TestCatch released(final Boolean released) {
        modify(RELEASED, released);
        return this;
    }

    @Override
    String getResourcePath() {
        return "/catches";
    }
}
