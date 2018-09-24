package uk.gov.defra.datareturns.test.licence;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.data.model.licences.Contact;
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
        Contact contact = crmLookupService.getContactFromLicence("B7A728");
        Assertions.assertThat(contact.getReturnStatus()).isEqualToIgnoringCase("success");
    }

    @Test
    public void testLicenceLookupFails() {
        Contact contact = crmLookupService.getContactFromLicence("B9A72D8");
        Assertions.assertThat(contact.getReturnStatus()).isEqualToIgnoringCase("error");
    }
}
