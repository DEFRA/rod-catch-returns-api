
package uk.gov.defra.datareturns.test.reporting;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.defra.datareturns.data.model.reporting.filters.SeasonFilter;

import java.time.Year;

public class SeasonFilterTests {
    @Test
    public void testSinglePositiveValue() {
        final SeasonFilter filter = new SeasonFilter("2018");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(2018);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(2018);

    }

    @Test
    public void testZeroValueUsesCurrentYear() {
        final SeasonFilter filter = new SeasonFilter("0");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(Year.now().getValue());
        Assertions.assertThat(filter.getEndYear()).isEqualTo(Year.now().getValue());
    }

    @Test
    public void testNegativeRange() {
        final SeasonFilter filter = new SeasonFilter("-2");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(Year.now().getValue() - 2);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(Year.now().getValue());
    }

    @Test
    public void testSpecificRange() {
        final SeasonFilter filter = new SeasonFilter("2016-2018");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(2016);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(2018);
    }

    @Test
    public void testSpecificRangeBackwards() {
        final SeasonFilter filter = new SeasonFilter("2018-2016");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(2016);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(2018);
    }


    @Test
    public void testInactiveAsterisk() {
        final SeasonFilter filter = new SeasonFilter("*");
        Assertions.assertThat(filter.isActive()).isEqualTo(false);
    }

    @Test
    public void testInactiveUnderscore() {
        final SeasonFilter filter = new SeasonFilter("_");
        Assertions.assertThat(filter.isActive()).isEqualTo(false);
    }

    @Test
    public void testInactiveNull() {
        final SeasonFilter filter = new SeasonFilter(null);
        Assertions.assertThat(filter.isActive()).isEqualTo(false);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvalidRange() {
        new SeasonFilter("abc");
    }
}
