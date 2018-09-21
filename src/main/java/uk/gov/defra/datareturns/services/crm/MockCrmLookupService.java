package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;

/**
 * Mock CRM lookup service
 *
 * @author Sam Gardner-Dell
 */
@Service
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "mock")
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Slf4j
@RequiredArgsConstructor
public class MockCrmLookupService implements CrmLookupService {

    @Override
    public Licence getLicence(final String lookup) {
        final Licence licence = new Licence();
        licence.setLicenceNumber("AABBCC");
        licence.setContact(getContact("1234567890"));
        return licence;
    }

    @Override
    public Contact getContact(final String contactId) {
        final Contact contact = new Contact();
        contact.setId(contactId);
        contact.setPostcode("WA4 1AB");
        return contact;
    }

    @Override
    public Contact getContactFromLicence(String licenceNumber) {
        return null;
    }
}
