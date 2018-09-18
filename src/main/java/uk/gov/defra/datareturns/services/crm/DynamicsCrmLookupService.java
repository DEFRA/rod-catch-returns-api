package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;

/**
 * Mock CRM lookup service
 *
 * @author Sam Gardner-Dell
 */
@Service
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Slf4j
@RequiredArgsConstructor
public class DynamicsCrmLookupService implements CrmLookupService {
    /**
     * the dynamics configuration
     */
    private DynamicsConfiguration dynamicsConfiguration;

    @Override
    public Licence getLicence(final String lookup) {
        // FIXME: Fetch the licence data from dynamic (dynamicsConfiguration.getEndpoint())
        final Licence licence = new Licence();
        licence.setLicenceNumber("CCBBAA");
        licence.setContact(getContact("0987654321"));
        return licence;
    }

    @Override
    public Contact getContact(final String contactId) {
        final Contact contact = new Contact();
        contact.setId(contactId);
        contact.setPostcode("WA4 1AB");
        return contact;
    }
}
