package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;

/**
 * Configuration options for validation
 *
 * @author Sam Gardner-Dell
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "validation")
@Getter
@Setter
@Validated
public class ValidatorConfiguration {
    /**
     * Enable/disable all validation functionality
     */
    private boolean enabled;

    @Max(value = 0, message = "Submission year offset must be 0 or negative.")
    private int submissionYearAllowedRangeOffset;
}
