package uk.gov.defra.datareturns.testutils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "authentication")
@Getter
@Setter
@ToString
public class AuthConfig {
    private String username;
    private String password;
}
