package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

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
     * The dynamics endpoint
     */
    private URI endpoint;

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
}
