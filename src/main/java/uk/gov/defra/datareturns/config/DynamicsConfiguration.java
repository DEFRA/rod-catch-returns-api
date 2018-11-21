package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.defra.datareturns.services.crm.DynamicsMockServer;
import uk.gov.defra.datareturns.services.crm.rest.ClientRestTemplateErrorHandler;
import uk.gov.defra.datareturns.services.crm.rest.ClientRestTemplateOAuth2ErrorHandler;
import uk.gov.defra.datareturns.services.crm.rest.IdentityRestTemplateErrorHandler;

import javax.validation.constraints.NotNull;
import java.net.URI;

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
     * @param details the oauth2 configuration to use for client credentials flow
     * @return an {@link OAuth2RestTemplate} preconfigured for OAuth 2.0 client credentials grant flow
     */
    @Bean
    protected RestTemplate dynamicsClientRestTemplate(final OAuth2ProtectedResourceDetails details) {
        RestTemplate template = buildClientCredentialsTemplate(details);
        if (DynamicsImpl.MOCK.equals(this.getImpl())) {
            template = new RestTemplate();
            DynamicsMockServer.bindTo(template);
        }
        template.setErrorHandler(new ClientRestTemplateErrorHandler());
        return template;
    }

    /**
     * Builds an {@link OAuth2RestTemplate} configured for client credentials flow.
     * Maps any OAuth2 request failure to an internal server error ensuring that these are properly logged.
     */
    private OAuth2RestTemplate buildClientCredentialsTemplate(final OAuth2ProtectedResourceDetails details) {
        final ClientCredentialsAccessTokenProvider provider = new ClientCredentialsAccessTokenProvider() {
            @Override
            protected ResponseErrorHandler getResponseErrorHandler() {
                return new ClientRestTemplateOAuth2ErrorHandler();
            }
        };
        final OAuth2RestTemplate template = new OAuth2RestTemplate(details);
        template.setAccessTokenProvider(provider);
        return template;
    }

    /**
     * @return RestTemplate for dynamics "identity" calls
     */
    @Bean
    protected RestTemplate dynamicsIdentityRestTemplate(final RestTemplateBuilder builder) {
        final RestTemplate template = builder.errorHandler(new IdentityRestTemplateErrorHandler()).build();
        if (DynamicsImpl.MOCK.equals(this.getImpl())) {
            DynamicsMockServer.bindTo(template);
        }
        return template;
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
        private URI url;

        /**
         * The dynamics api path
         */
        @NotNull
        private String apiPath;


        /**
         * Retrieve the URI for the given dynamics stored procedure
         *
         * @param storedProcedureName the stored procedure name
         * @return the request URI for the stored procedure
         */
        public URI getApiStoredProcedureEndpoint(final String storedProcedureName) {
            return UriComponentsBuilder.fromUri(getUrl()).path(getApiPath()).path("/").path(storedProcedureName).build().toUri();
        }
    }
}
