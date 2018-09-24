package uk.gov.defra.datareturns.services.crm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
import uk.gov.defra.datareturns.data.model.licences.Contact;
import uk.gov.defra.datareturns.services.aad.TokenService;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

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

    /**
     * The dynamics configuration
     */
    private DynamicsConfiguration dynamicsConfiguration;

    /**
     * The dynamics authentication token service
     */
    private TokenService tokenService;

    @Inject
    public DynamicsCrmLookupService(DynamicsConfiguration dynamicsConfiguration, TokenService tokenService) {
        this.dynamicsConfiguration = dynamicsConfiguration;
        this.tokenService = tokenService;
    }

    //TODO Implement
    @Override
    public Contact getContact(final String contactId) {
        final Contact contact = new Contact();
        contact.setId(contactId);
        contact.setPostcode("WA4 1AB");
        return contact;
    }

    @Override
    public Contact getContactFromLicence(String licenceNumber) {
        ContactQuery contactQuery = new ContactQuery();
        ContactQuery.Query query = new ContactQuery.Query();
        query.setPermissionNumber(licenceNumber);
        contactQuery.setQuery(query);
        return callCRM(contactQuery);
    }

    /**
     * This interface defines classes that describe the artifacts needed
     * to call a stored procedure on the CRM, that is the query parameters
     * posted to the CRM in the payload, the stored procedure name and
     * the resulting type - a class extending CRMEntity
     * @param <T>
     */
    public interface CRMQuery<T extends CRMEntity> {
        String getCRMStoredProcedureName();
        CRMQuery.Query getQuery();
        Class<T> getEntityClass();
        interface Query {}
    }

    /**
     * This Query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class ContactQuery implements CRMQuery<Contact> {
        private final String cRMStoredProcedureName = "defra_GetContactByLicenseNumber";
        private Query query;

        public Class<Contact> getEntityClass() {
            return Contact.class;
        }

        @Getter
        @Setter
        @ToString
        public static class Query implements CRMQuery.Query {
            @JsonProperty("PermissionNumber")
            private String permissionNumber;
        }
    }

    /**
     * Generic CRM call method - uses the spring rest template
     * @param crmQuery
     * @param <T>
     * @return
     */
    private <T extends CRMEntity> T callCRM(CRMQuery<T> crmQuery) {
        try {
            URL url = new URL(dynamicsConfiguration.getEndpoint(),
                    dynamicsConfiguration.getApi().toString() + "/" + crmQuery.getCRMStoredProcedureName());

            String urlString = url.toString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + tokenService.getToken());
            HttpEntity<CRMQuery.Query> entity = new HttpEntity<>(crmQuery.getQuery(), headers);
            RestTemplate restTemplate = new RestTemplate();

            return restTemplate.postForObject(
                    urlString,
                    entity, crmQuery.getEntityClass());

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
