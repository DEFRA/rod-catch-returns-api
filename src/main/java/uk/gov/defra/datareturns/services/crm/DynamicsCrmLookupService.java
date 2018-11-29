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

import javax.inject.Provider;
import java.net.URI;

import static uk.gov.defra.datareturns.services.crm.entity.CrmActivity.Status.STARTED;
import static uk.gov.defra.datareturns.services.crm.entity.CrmActivity.Status.SUBMITTED;

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

    /**
     * Rest template provider for CRM calls using client-credentials flow.
     */
    private final Provider<RestTemplate> dynamicsClientRestTemplate;

    /**
     * Rest template provider for CRM calls using a token retrieved via resource owner credentials flow.
     */
    private final Provider<RestTemplate> dynamicsIdentityRestTemplate;

    /**
     * The dynamics authentication token service
     */
    private final TokenService tokenService;

    @Override
    public Licence getLicence(final String licenceNumber, final String postcode) {
        final CrmLicence.LicenceQuery licenceQuery = new CrmLicence.LicenceQuery();
        licenceQuery.setQueryParams(CrmLicence.QueryParams.of(licenceNumber, postcode));
        return callCRM(dynamicsClientRestTemplate.get(), licenceQuery, null);
    }

    @Override
    public void createActivity(final String contactId, final short season) {
        final CrmActivity.CreateActivity createActivity = new CrmActivity.CreateActivity();
        createActivity.setQueryParams(CrmActivity.QueryParams.of(STARTED, contactId, season));
        callCRM(dynamicsClientRestTemplate.get(), createActivity, null);
    }

    @Override
    public void updateActivity(final String contactId, final short season) {
        final CrmActivity.UpdateActivity updateActivity = new CrmActivity.UpdateActivity();
        updateActivity.setQueryParams(CrmActivity.QueryParams.of(SUBMITTED, contactId, season));
        callCRM(dynamicsClientRestTemplate.get(), updateActivity, null);
    }

    @Override
    public Identity getAuthenticatedUserRoles(final String username, final String password) {
        final CrmIdentity.IdentityQuery identityQuery = new CrmIdentity.IdentityQuery();
        final String token = tokenService.getTokenForUserIdentity(username, password);
        return callCRM(dynamicsIdentityRestTemplate.get(), identityQuery, token);
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
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        final HttpEntity<?> requestEntity = new HttpEntity<>(crmQuery.getQueryParams(), headers);
        final URI storedProcedure = endpointConfiguration.getApiStoredProcedureEndpoint(crmQuery.getCRMStoredProcedureName());
        final CrmCall<B> result = restTemplate.postForObject(storedProcedure, requestEntity, crmQuery.getEntityClass());
        B entity = null;
        if (result != null) {
            entity = result.getBaseEntity();
        }
        return entity;
    }
}
