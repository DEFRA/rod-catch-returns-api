package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URL;

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

    @Configuration
    @ConfigurationProperties(prefix = "dynamics.endpoint")
    @ConditionalOnProperty(name = "dynamics.impl", havingValue = "DYNAMICS")
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
    }
}
