package uk.gov.defra.datareturns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Application class for the Rod catch returns API
 *
 * @author Sam Gardner-Dell
 */
@SpringBootApplication
@EnableCaching
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor", "NonFinalUtilityClass"})
public class RcrApi {
    /**
     * Application main startup method
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(RcrApi.class, args);
    }
}
