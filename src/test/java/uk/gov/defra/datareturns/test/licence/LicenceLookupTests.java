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
        final Licence licence = crmLookupService.getLicence("B7A728", "WA4 8HT");
        Assertions.assertThat(licence).isNotNull();
    }

    @Test
    public void testLicenceLookupFailsPostcodeMismatch() {
        final Licence licence = crmLookupService.getLicence("B91235", "WA4 0HT");
        Assertions.assertThat(licence).isNull();
    }

    @Test
    public void testLicenceLookupFailsUnknownLicence() {
        final Licence licence = crmLookupService.getLicence("B9A72DD", "WA4 1HT");
        Assertions.assertThat(licence).isNull();
    }
}
