package uk.gov.defra.datareturns.test.licence;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.crm.MockCrmLookupService;
import uk.gov.defra.datareturns.testcommons.framework.WebIntegrationTest;

import javax.inject.Inject;

/**
 * Integration tests licence lookup
 */
@RunWith(SpringRunner.class)
@WebIntegrationTest
@Slf4j
public class TestLicenceLookup {
    @Inject
    private MockCrmLookupService crmLookupService;

    @Test
    public void testLicenceLookupSucceds() {
        Licence licence = crmLookupService.getLicenceFromLicenceNumber("B7A728");
        Assertions.assertThat(licence).isNotNull();
    }

    @Test
    public void testLicenceLookupFails() {
        Licence licence = crmLookupService.getLicenceFromLicenceNumber("B9A72D8");
        Assertions.assertThat(licence).isNull();
    }
}
