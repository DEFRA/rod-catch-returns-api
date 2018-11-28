package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.aad.TokenService;
import uk.gov.defra.datareturns.services.crm.entity.CrmActivity;
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
    public void createActivity(final String contactId, final short season) {
        final CrmActivity.CreateActivity createActivity = new CrmActivity.CreateActivity();
        final CrmActivity.Query query = new CrmActivity.Query();
        query.setStatus(CrmActivity.Status.STARTED);
        query.setContactId(contactId);
        query.setSeason(season);
        createActivity.setQuery(query);
        callCRM(dynamicsClientRestTemplate, createActivity, null);
    }

    @Override
    public void updateActivity(final String contactId, final short season) {
        final CrmActivity.UpdateActivity updateActivity = new CrmActivity.UpdateActivity();
        final CrmActivity.Query query = new CrmActivity.Query();
        query.setStatus(CrmActivity.Status.SUBMITTED);
        query.setContactId(contactId);
        query.setSeason(season);
        updateActivity.setQuery(query);
        callCRM(dynamicsClientRestTemplate, updateActivity, null);
    }

    @Override
    public Identity getAuthenticatedUserRoles(final String username, final String password) {
        final CrmIdentity.IdentityQuery identityQuery = new CrmIdentity.IdentityQuery();
        final String token = tokenService.getTokenForUserIdentity(username, password);
        return callCRM(dynamicsIdentityRestTemplate, identityQuery, token);
    }

    /**
     * Generic CRM call method - uses the spring rest template
     *
     * @param crmQuery - the instance of the CRM query
     * @param <T>      - The type of the returned entity
     * @return - The returned entity object from the CRM
     */
    private <B, T extends CrmCall<B>> B callCRM(final RestTemplate restTemplate, final CrmCall.CRMQuery<T> crmQuery,
                                                final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        final HttpEntity<?> requestEntity = new HttpEntity<>(crmQuery.getQuery(), headers);
        final URI storedProcedure = endpointConfiguration.getApiStoredProcedureEndpoint(crmQuery.getCRMStoredProcedureName());
        final CrmCall<B> result = restTemplate.postForObject(storedProcedure, requestEntity, crmQuery.getEntityClass());
        B entity = null;
        if (result != null) {
            entity = result.getBaseEntity();
        }
        return entity;
    }
}
