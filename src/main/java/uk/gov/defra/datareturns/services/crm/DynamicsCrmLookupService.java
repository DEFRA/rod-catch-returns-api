package uk.gov.defra.datareturns.services.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.aad.TokenService;
import uk.gov.defra.datareturns.services.crm.entity.CrmActivity;
import uk.gov.defra.datareturns.services.crm.entity.CrmCall;
import uk.gov.defra.datareturns.services.crm.entity.CrmLicence;
import uk.gov.defra.datareturns.services.crm.entity.CrmResponseEntity;
import uk.gov.defra.datareturns.services.crm.entity.CrmRoles;

import javax.inject.Provider;
import javax.validation.Validator;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * Bean validator
     */
    private final Validator validator;

    /**
     * The dynamics authentication token service
     */
    private final TokenService tokenService;

    @Override
    public Optional<Licence> getLicence(final String licenceNumber, final String postcode) {
        final CrmLicence.LicenceQuery licenceQuery = new CrmLicence.LicenceQuery();
        licenceQuery.setQueryParams(CrmLicence.QueryParams.of(licenceNumber, postcode));
        return callCRM(dynamicsClientRestTemplate.get(), licenceQuery, null);
    }

    @Override
    public Optional<Licence> getLicence(final String fullLicenceNumber) {
        String entity = "defra_permissions";
        MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
        queryMap.add("$filter", "defra_name eq '" + fullLicenceNumber + "'");
        queryMap.add("$expand", "defra_ContactId");
        queryMap.add("$select", "defra_name");

        Optional<CrmResponseEntity> response = callCRMWithQueryString(
                dynamicsClientRestTemplate.get(), entity, queryMap, CrmResponseEntity.class, null);

        Optional<Licence> result = Optional.empty();
        if (response.isPresent() && response.get().getValue() != null && !response.get().getValue().isEmpty()) {
            Licence licence = new Licence();
            licence.setLicenceNumber(response.get().getValue().get(0).getPermissionNumber());
            final Contact contact = new Contact();
            contact.setId(response.get().getValue().get(0).getContact().getContactId());
            contact.setFullName(response.get().getValue().get(0).getContact().getFullName());
            licence.setContact(contact);
            result = Optional.ofNullable(licence);
        }
        return result;
    }

    @Override
    public void createActivity(final String contactId, final short season) {
        final CrmActivity.CrmActivityQuery query = new CrmActivity.CrmActivityQuery(STARTED, contactId, season);
        callCRM(dynamicsClientRestTemplate.get(), query, null);
    }

    @Override
    public void updateActivity(final String contactId, final short season) {
        final CrmActivity.CrmActivityQuery query = new CrmActivity.CrmActivityQuery(SUBMITTED, contactId, season);
        callCRM(dynamicsClientRestTemplate.get(), query, null);
    }

    @Override
    @NonNull
    public List<String> getAuthenticatedUserRoles(final String username, final String password) {
        final CrmRoles.CrmRolesQuery crmRolesQuery = new CrmRoles.CrmRolesQuery();
        final String token = tokenService.getTokenForUserIdentity(username, password);
        return callCRM(dynamicsIdentityRestTemplate.get(), crmRolesQuery, token).orElse(Collections.emptyList());
    }

    /**
     * Generic CRM call method - uses the spring rest template
     *
     * @param crmQuery - the instance of the CRM query
     * @param <T>      - The type of the returned entity
     * @return - The returned entity object from the CRM
     */
    private <B, T extends CrmCall<B>> Optional<B> callCRM(final RestTemplate restTemplate, final CrmCall.CRMQuery<T> crmQuery,
                                                          final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        final HttpEntity<?> requestEntity = new HttpEntity<>(crmQuery.getQueryParams(), headers);
        final URI storedProcedure = endpointConfiguration.getApiStoredProcedureEndpoint(crmQuery.getQueryName());
        final CrmCall<B> response = restTemplate.postForObject(storedProcedure, requestEntity, crmQuery.getEntityClass());

        Optional<B> result = Optional.empty();
        if (response != null && validator.validate(response).isEmpty()) {
            result = Optional.ofNullable(response.getBaseEntity());
        }
        return result;
    }

    /**
     * Generic CRM call method with query - uses the spring rest template
     *
     * @param queryMap - a map representing the query
     * @param <T>  - The type of the returned entity
     * @return - The returned entity object from the CRM
     */
    private <T> Optional<T> callCRMWithQueryString(
            final RestTemplate restTemplate, final String entity, final MultiValueMap<String, String> queryMap,
            final Class<T> responseType, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        final HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        final URI url = endpointConfiguration.getApiQueryEndpoint(entity, queryMap);

        final ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

        Optional<T> result = Optional.empty();
        if (response.getBody() != null && validator.validate(response).isEmpty()) {
            result = Optional.ofNullable(response.getBody());
        }
        return result;
    }
}
