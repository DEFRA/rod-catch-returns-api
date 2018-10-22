package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.data.model.licences.Activity;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.data.model.licences.MockLicenceData;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock CRM lookup service
 *
 * @author Sam Gardner-Dell
 */
@Service
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "MOCK")
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Slf4j
@RequiredArgsConstructor
public class MockCrmLookupService implements CrmLookupService {
    private static final Pattern USER_PTN = Pattern.compile("(?i)admin.*@example.com");


    @Override
    public Licence getLicenceFromLicenceNumber(final String licenceNumber) {
        if (MockLicenceData.LICENCES.containsKey(licenceNumber.toUpperCase().trim())) {
            return MockLicenceData.LICENCES.get(licenceNumber);
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
        final Matcher userMatcher = USER_PTN.matcher(username);
        if (userMatcher.matches() && password.contains("admin")) {
            final Identity identity = new Identity();
            identity.setRoles(new HashSet<>(Collections.singletonList("RcrAdminUser")));
            return identity;
        }
        return null;
    }
}
