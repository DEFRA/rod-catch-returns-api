package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URL;

/**
 * Configuration options for Microsoft Azure active directory authenticating authority
 *
 * @author Sam Gardner-Dell
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "active-directory")
@Getter
@Setter
@Validated
public class AADConfiguration {
    private URI tenant;
    private URL authority;
    private String clientId;
    private String clientSecret;
}
