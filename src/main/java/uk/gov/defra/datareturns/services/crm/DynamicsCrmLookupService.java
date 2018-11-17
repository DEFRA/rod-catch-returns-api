package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;
import uk.gov.defra.datareturns.data.model.licences.Activity;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.aad.TokenService;
import uk.gov.defra.datareturns.services.crm.entity.CrmActivity;
import uk.gov.defra.datareturns.services.crm.entity.CrmBaseEntity;
import uk.gov.defra.datareturns.services.crm.entity.CrmCall;
import uk.gov.defra.datareturns.services.crm.entity.CrmIdentity;
import uk.gov.defra.datareturns.services.crm.entity.CrmLicence;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import java.net.URI;

/**
 * CRM lookup service
 *
 * @author Graham Willis
 */
@Service
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
@RequiredArgsConstructor
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Slf4j
public class DynamicsCrmLookupService implements CrmLookupService {
    /**
     * The dynamics configuration
     */
    private final DynamicsConfiguration.Endpoint endpointConfiguration;
    private final RestTemplate dynamicsClientRestTemplate;
    private final RestTemplate dynamicsIdentityRestTemplate;

    /**
     * The dynamics authentication token service
     */
    private final TokenService tokenService;

    @Override
    public Licence getLicenceFromLicenceNumber(final String licenceNumber) {
        final CrmLicence.LicenceQuery licenceQuery = new CrmLicence.LicenceQuery();
        final CrmLicence.LicenceQuery.Query query = new CrmLicence.LicenceQuery.Query();
        query.setPermissionNumber(licenceNumber);
        licenceQuery.setQuery(query);
        return callCRM(dynamicsClientRestTemplate, licenceQuery, null);
    }

    @Override
    public Activity createActivity(final String contactId, final short season) {
        final CrmActivity.CreateActivity createActivity = new CrmActivity.CreateActivity();
        log.debug("Creating activity on contact: " + contactId);
        final CrmActivity.CreateActivity.Query query = new CrmActivity.CreateActivity.Query();
        query.setContactId(contactId);
        query.setSeason(season);
        createActivity.setQuery(query);
        return callCRM(dynamicsClientRestTemplate, createActivity, null);
    }

    @Override
    public Activity updateActivity(final String contactId, final short season) {
        final CrmActivity.UpdateActivity updateActivity = new CrmActivity.UpdateActivity();
        log.debug("Updating activity on contact: " + contactId);
        final CrmActivity.UpdateActivity.Query query = new CrmActivity.UpdateActivity.Query();
        query.setContactId(contactId);
        query.setSeason(season);
        updateActivity.setQuery(query);
        return callCRM(dynamicsClientRestTemplate, updateActivity, null);
    }

    @Override
    public Identity getAuthenticatedUserRoles(final String username, final String password) {
        final CrmIdentity.IdentityQuery identityQuery = new CrmIdentity.IdentityQuery();
        final String token = getIdentityToken(username, password);
        if (token == null) {
            return null;
        }
        return callCRM(dynamicsIdentityRestTemplate, identityQuery, token);
    }

    String getIdentityToken(final String username, final String password) {
        log.debug("Getting access token using resource owner credentials flow for user: " + username);
        return tokenService.getTokenForUserIdentity(username, password);
    }

    /**
     * Generic CRM call method - uses the spring rest template
     *
     * @param crmQuery - the instance of the CRM query
     * @param <T>      - The type of the returned entity
     * @return - The returned entity object from the CRM
     */
    private <B extends CrmBaseEntity, T extends CrmCall<B>> B callCRM(final RestTemplate restTemplate, final CrmCall.CRMQuery<T> crmQuery,
                                                                      final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        final HttpEntity<CrmCall.CRMQuery.Query> entity = new HttpEntity<>(crmQuery.getQuery(), headers);
        final URI storedProcedure = endpointConfiguration.getApiStoredProcedureEndpoint(crmQuery.getCRMStoredProcedureName());
        final CrmCall<B> result = restTemplate.postForObject(storedProcedure, entity, crmQuery.getEntityClass());
        if (result == null) {
            return null;
        }
        return result.getBaseEntity();
    }
}
