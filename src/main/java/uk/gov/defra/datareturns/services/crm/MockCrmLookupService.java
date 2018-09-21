package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static final Map<String, Contact> licences = new HashMap<>();
    private static Map<String, Contact> byContact;

    static {
        for (int i = 1; i <= 8; i++) {
            Contact c = new Contact();
            c.setPostcode(String.format("WA4 %dHT", i));
            c.setId(String.format("f8e6ee6a-8fba-e811-a96c-000%d3ab9add5", i));
            c.setReturnStatus("success");
            licences.put(String.format("B7A7%d8", i), c);
        }
        byContact = licences.values()
                .stream()
                .collect(Collectors.toMap(Contact::getId, c -> c));
    }

    @Override
    public Contact getContact(final String contactId) {
        if (byContact.containsKey(contactId)) {
            return byContact.get(contactId);
        } else {
            final Contact contact = new Contact();
            contact.setReturnStatus("error");
            contact.setErrorMessage("Contact not found");
            return contact;
        }
    }

    @Override
    public Contact getContactFromLicence(String licenceNumber) {
        if (licences.containsKey(licenceNumber.toUpperCase().trim())) {
            return licences.get(licenceNumber);
        } else {
            final Contact contact = new Contact();
            contact.setReturnStatus("error");
            contact.setErrorMessage("Contact not found");
            return contact;
        }
    }

    @Override
    public Contact getContactFromLicence(String licenceNumber) {
        return null;
    }
}
