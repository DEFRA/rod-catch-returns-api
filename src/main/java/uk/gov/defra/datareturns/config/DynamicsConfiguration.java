package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
        if (DynamicsImpl.MOCK.equals(this.getImpl())) {
            return new RestTemplate();
        }
        return new OAuth2RestTemplate(details);
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
