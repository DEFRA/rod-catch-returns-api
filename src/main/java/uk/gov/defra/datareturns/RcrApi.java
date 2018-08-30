package uk.gov.defra.datareturns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application class for the Data Returns PI submissions API.
 *
 * @author Sam Gardner-Dell
 */
@SpringBootApplication
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
