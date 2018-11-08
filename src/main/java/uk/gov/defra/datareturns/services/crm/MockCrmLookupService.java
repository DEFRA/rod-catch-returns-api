package uk.gov.defra.datareturns.services.crm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;

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
public class MockCrmLookupService extends DynamicsCrmLookupService implements CrmLookupService {
    public static final String MOCK_BEARER_TOKEN = "MOCK_BEARER_TOKEN";
    private static final Pattern USER_PTN = Pattern.compile("(?i)admin.*@example.com");

    public MockCrmLookupService(final DynamicsConfiguration.Endpoint endpointConfiguration,
                                final RestTemplate dynamicsClientRestTemplate,
                                final RestTemplate dynamicsIdentityRestTemplate) {
        super(endpointConfiguration, dynamicsClientRestTemplate, dynamicsIdentityRestTemplate, null);
        DynamicsMockServer.bindTo(dynamicsClientRestTemplate);
        DynamicsMockServer.bindTo(dynamicsIdentityRestTemplate);
    }

    @Override
    String getIdentityToken(final String username, final String password) {
        final Matcher userMatcher = USER_PTN.matcher(username);
        if (userMatcher.matches() && password.contains("admin")) {
            return MOCK_BEARER_TOKEN;
        }
        return null;
    }
}
