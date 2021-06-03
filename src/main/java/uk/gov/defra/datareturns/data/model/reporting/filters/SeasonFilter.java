package uk.gov.defra.datareturns.data.model.reporting.filters;

import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class SeasonFilter extends AbstractPathVariableFilter<Short> {
    private static final Pattern PTN = Pattern.compile("^(?<single>-?\\d+)|(?<low>\\d+)-(?<high>\\d+)$");
    private int startYear = Year.now().getValue();
    private int endYear = Year.now().getValue();

    public SeasonFilter(final String seasonFilter) {
        super(seasonFilter);
        if (isActive()) {
            final Matcher m = PTN.matcher(seasonFilter);
            if (!m.matches()) {
                throw new UnsupportedOperationException("Invalid season filter");
            }
            final String single = m.group("single");
            final String low = m.group("low");
            final String high = m.group("high");

            if (single != null) {
                final int s = Integer.parseInt(single);
                if (s < 1) {
                    // Temporary change while we continue to use Excel reports
                    // Negative parameters adjusted to reduce data by 1 year
                    final int adjustedYears = (s == 0) ? 0 : Math.abs(s) - 1;
                    startYear = Year.now().minusYears(adjustedYears).getValue();
                    endYear = Year.now().getValue();
                } else {
                    startYear = s;
                    endYear = s;
                }
            } else {
                final int lowVal = Integer.parseInt(low);
                final int highVal = Integer.parseInt(high);
                startYear = Math.min(lowVal, highVal);
                endYear = Math.max(lowVal, highVal);
            }
        }
    }

    @Override
    Predicate toPredicate(final CriteriaBuilder cb, final Path<Short> path) {
        return cb.between(path, (short) getStartYear(), (short) getEndYear());
    }

    @Override
    public String toString() {
        return startYear + "-" + endYear;
    }
}
