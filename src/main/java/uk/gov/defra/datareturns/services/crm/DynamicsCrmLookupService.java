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
import uk.gov.defra.datareturns.services.crm.entity.CrmBaseEntity;
import uk.gov.defra.datareturns.services.crm.entity.CrmActivity;
import uk.gov.defra.datareturns.services.crm.entity.CrmCall;
import uk.gov.defra.datareturns.services.crm.entity.CrmIdentity;
import uk.gov.defra.datareturns.services.crm.entity.CrmLicence;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * CRM lookup service
 *
 * @author Graham Willis
 */
@Service
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Slf4j
@RequiredArgsConstructor
public class DynamicsCrmLookupService implements CrmLookupService {

    // The dynamics configuration
    private DynamicsConfiguration dynamicsConfiguration;


    // The dynamics authentication token service
    private TokenService tokenService;

    // A CRM query to get the licence and contact details
    private final CrmLicence.LicenceQuery licenceQuery = new CrmLicence.LicenceQuery();

    // A CRM query to create the activity status
    private final CrmActivity.CreateActivity createActivity = new CrmActivity.CreateActivity();

    // A CRM query to update the activity status
    private final CrmActivity.UpdateActivity updateActivity = new CrmActivity.UpdateActivity();

    // A CRM query to get teh internal user identity
    private final CrmIdentity.IdentityQuery identityQuery = new CrmIdentity.IdentityQuery();

    @Inject
    public DynamicsCrmLookupService(final DynamicsConfiguration dynamicsConfiguration, final TokenService tokenService) {
        this.dynamicsConfiguration = dynamicsConfiguration;
        this.tokenService = tokenService;

    }

    @Override
    public Licence getLicenceFromLicenceNumber(final String licenceNumber) {
        CrmLicence.LicenceQuery.Query query = new CrmLicence.LicenceQuery.Query();
        query.setPermissionNumber(licenceNumber);
        licenceQuery.setQuery(query);
        return Objects.requireNonNull(callCRM(licenceQuery, tokenService.getToken()));
    }

    @Override
    public Activity createActivity(final String contactId, final short season) {
        log.debug("Creating activity on contact: " + contactId);
        CrmActivity.CreateActivity.Query query = new CrmActivity.CreateActivity.Query();
        query.setContactId(contactId);
        query.setSeason(season);
        createActivity.setQuery(query);
        return Objects.requireNonNull(callCRM(createActivity, tokenService.getToken()));
    }

    @Override
    public Activity updateActivity(final String contactId, final short season) {
        log.debug("Updating activity on contact: " + contactId);
        CrmActivity.UpdateActivity.Query query = new CrmActivity.UpdateActivity.Query();
        query.setContactId(contactId);
        query.setSeason(season);
        updateActivity.setQuery(query);
        return Objects.requireNonNull(callCRM(updateActivity, tokenService.getToken()));
    }

    @Override
    public Identity getAuthenticatedUserRoles(final String username, final String password) {
        log.debug("Getting identity for user: " + username);
        final String token = tokenService.getTokenForUserIdentity(username, password);
        if (token == null) {
            return null;
        } else {
            return Objects.requireNonNull(callCRM(identityQuery, token));
        }
    }

    /**
     * Generic CRM call method - uses the spring rest template
     * @param crmQuery - the instance of the CRM query
     * @param <T> - The type of the returned entity
     * @return - The returned entity object from the CRM
     */
    private <B extends CrmBaseEntity, T extends CrmCall<B>> B callCRM(final CrmCall.CRMQuery<T> crmQuery, final String token) {
        try {
            URL url = new URL(dynamicsConfiguration.getEndpoint(),
                    dynamicsConfiguration.getApi().toString() + "/" + crmQuery.getCRMStoredProcedureName());

            String urlString = url.toString();
            log.debug("CRM Query: " + urlString);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<CrmCall.CRMQuery.Query> entity;

            if (crmQuery.getQuery() != null) {
                entity = new HttpEntity<>(crmQuery.getQuery(), headers);
                log.debug("Payload: " + crmQuery.getQuery());
            } else {
                entity = new HttpEntity<>(headers);
            }

            RestTemplate restTemplate = new RestTemplate();

            T result = restTemplate.postForObject(
                    urlString,
                    entity, crmQuery.getEntityClass());

            return result.getBaseEntity();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
