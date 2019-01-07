package uk.gov.defra.datareturns.test.reporting;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.defra.datareturns.data.model.reporting.filters.SeasonFilter;

import java.time.Year;

public class SeasonFilterTests {
    @Test
    public void testSinglePositiveValue() {
        SeasonFilter filter = new SeasonFilter("2018");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(2018);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(2018);

    }

    @Test
    public void testZeroValueUsesCurrentYear() {
        SeasonFilter filter = new SeasonFilter("0");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(Year.now().getValue());
        Assertions.assertThat(filter.getEndYear()).isEqualTo(Year.now().getValue());
    }

    @Test
    public void testNegativeRange() {
        SeasonFilter filter = new SeasonFilter("-2");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(Year.now().getValue() - 2);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(Year.now().getValue());
    }

    @Test
    public void testSpecificRange() {
        SeasonFilter filter = new SeasonFilter("2016-2018");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(2016);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(2018);
    }

    @Test
    public void testSpecificRangeBackwards() {
        SeasonFilter filter = new SeasonFilter("2018-2016");
        Assertions.assertThat(filter.getStartYear()).isEqualTo(2016);
        Assertions.assertThat(filter.getEndYear()).isEqualTo(2018);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvalidRange() {
        new SeasonFilter("abc");
    }
}
