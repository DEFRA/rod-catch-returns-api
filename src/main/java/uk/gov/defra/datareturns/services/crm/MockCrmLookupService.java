package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.data.model.licences.Activity;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import java.util.HashMap;
import java.util.Map;

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

    private static final Map<String, Licence> LICENCES = new HashMap<>();

    static {
        for (int i = 1; i <= 8; i++) {
            Licence l = new Licence();

            String permission = String.format("B7A7%d8", i);
            String postcode = String.format("WA4 %dHT", i);

            l.setLicenceNumber(permission);
            Contact c = new Contact();

            c.setPostcode(postcode);
            c.setId(String.format("f8e6ee6a-8fba-e811-a96c-000%d3ab9add5", i));

            l.setContact(c);
            LICENCES.put(permission, l);
            log.info("Mock licence: " + l);
        }
    }

    @Override
    public Licence getLicenceFromLicenceNumber(final String licenceNumber) {
        if (LICENCES.containsKey(licenceNumber.toUpperCase().trim())) {
            return LICENCES.get(licenceNumber);
        } else {
            return null;
        }
    }

    @Override
    public Activity createActivity(final String contactId, final short season) {
        log.debug("Mock: Creating activity on contact: " + contactId);
        return null;
    }

    @Override
    public Activity updateActivity(final String contactId, final short season) {
        log.debug("Mock: Updating activity on contact: " + contactId);
        return null;
    }

    @Override
    public Identity getAuthenticatedUserRoles(final String username, final String password) {
        log.debug("Mock: finding roles for user: " + username);
        return new Identity();
    }
}
