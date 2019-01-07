package uk.gov.defra.datareturns.test.catches;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.catches.CatchMass;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import java.math.BigDecimal;

/**
 * Unit tests for the {@link CatchMass} class
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@WithAdminUser
@Slf4j
public class CatchMassTests {

    @Test
    public void testOzToKg() {
        final CatchMass mass = new CatchMass();
        mass.set(CatchMass.MeasurementType.IMPERIAL, BigDecimal.ONE);
        mass.conciliateMass();
        Assertions.assertThat(mass.getKg()).isEqualByComparingTo(new BigDecimal("0.028349523125"));
    }

    @Test
    public void testKgToOz() {
        final CatchMass mass = new CatchMass();
        mass.set(CatchMass.MeasurementType.METRIC, BigDecimal.ONE);
        mass.conciliateMass();
        Assertions.assertThat(mass.getOz()).isEqualByComparingTo(new BigDecimal("35.273962"));
    }
}
