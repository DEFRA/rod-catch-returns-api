package uk.gov.defra.datareturns.test.licence;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Integration tests licence lookup
 */
@RunWith(SpringRunner.class)
@ApiContextTest
@Slf4j
public class LicenceLookupTests {
    @Inject
    private CrmLookupService crmLookupService;

    @Test
    public void testLicenceLookupSucceeds() {
        final Optional<Licence> licence = crmLookupService.getLicence("B7A728", "WA4 8HT");
        Assertions.assertThat(licence).isPresent();
    }

    @Test
    public void testLicenceLookupFailsPostcodeMismatch() {
        final Optional<Licence> licence = crmLookupService.getLicence("B91235", "WA4 0HT");
        Assertions.assertThat(licence).isNotPresent();
    }

    @Test
    public void testLicenceLookupFailsUnknownLicence() {
        final Optional<Licence> licence = crmLookupService.getLicence("B9A72DD", "WA4 1HT");
        Assertions.assertThat(licence).isNotPresent();
    }

    @Test
    public void testFullLicenceLookupSucceeds() {
        final Optional<Licence> licence = crmLookupService.getLicence("00081019-1WS3JP4-B7A718");
        Assertions.assertThat(licence).isPresent();
    }

    @Test
    public void testFullLicenceLookupFails() {
        final Optional<Licence> licence = crmLookupService.getLicence("00081019");
        Assertions.assertThat(licence).isNotPresent();
    }
}
