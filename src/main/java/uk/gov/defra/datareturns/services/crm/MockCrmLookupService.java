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
        log.debug("Mock: finding roles for user: " + username);
        return new Identity();
    }
}
