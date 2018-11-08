package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

/**
 * Configuration options for Microsoft Dynamics integration
 *
 * @author Sam Gardner-Dell
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dynamics")
@Getter
@Setter
@Validated
public class DynamicsConfiguration {
    /**
     * The service implementation to use - supported values are 'dynamics' and 'mock'
     */
    @NotNull
    private DynamicsImpl impl;

    /**
     * Retrieve an {@link OAuth2RestTemplate} preconfigured for OAuth 2.0 client credentials grant flow
     *
     * @param aadConfiguration the oauth2 configuration to use for client credentials flow
     * @param endpoint         the dynamics endpoint configuration
     * @return an {@link OAuth2RestTemplate} preconfigured for OAuth 2.0 client credentials grant flow
     * @throws IOException if a configuration error prevents the template from being created
     */
    @Bean
    @ConditionalOnBean(AADConfiguration.class)
    protected RestTemplate dynamicsClientRestTemplate(final AADConfiguration aadConfiguration,
                                                      final DynamicsConfiguration.Endpoint endpoint) throws IOException {
        final ClientCredentialsResourceDetails credentials = new ClientCredentialsResourceDetails();
        credentials.setClientId(aadConfiguration.getClientId());
        credentials.setClientSecret(aadConfiguration.getClientSecret());
        credentials.setAccessTokenUri(aadConfiguration.getTenantedLoginUrl() + "/oauth2/v2.0/token");
        credentials.setClientAuthenticationScheme(AuthenticationScheme.form);
        credentials.setScope(Collections.singletonList(endpoint.getDefaultOAuth2Scope()));
        final OAuth2RestTemplate template = new OAuth2RestTemplate(credentials);
        template.setAccessTokenProvider(new ClientCredentialsAccessTokenProvider());
        return template;
    }


    /**
     * @return RestTemplate for dynamics "identity" calls
     */
    @Bean
    protected RestTemplate dynamicsIdentityRestTemplate() {
        return new RestTemplate();
    }


    /**
     * Available dynamics implementations
     */
    public enum DynamicsImpl {
        /**
         * the configured dynamics endpoint will be queried
         */
        DYNAMICS,
        /**
         * no connectivity to dynamics - mock data will be used
         */
        MOCK
    }

    /**
     * Dynamics endpoint configuration
     */
    @Configuration
    @ConfigurationProperties(prefix = "dynamics.endpoint")
    @Getter
    @Setter
    @Validated
    public static class Endpoint {
        /**
         * The dynamics endpoint url
         */
        @NotNull
        private URL url;

        /**
         * The dynamics api path
         */
        @NotNull
        private URI apiPath;

        /**
         * @return the scope to use with OAuth2 client credentials flow
         * @throws MalformedURLException on configuration error
         */
        public String getDefaultOAuth2Scope() throws MalformedURLException {
            return new URL(url, ".default").toString();
        }
    }
}
