package uk.gov.defra.datareturns.services.crm;

import org.hibernate.validator.constraints.Range;

import java.util.regex.Pattern;

/**
 * Provides mock data for use when running with the service set to use the mock dynamics implementation
 * This enables the API to be started in a local "mock" mode with known test data.
 *
 * @author Sam Gardner-Dell
 * @see uk.gov.defra.datareturns.config.DynamicsConfiguration
 */
public final class DynamicsMockData {
    /**
     * pattern to match valid mock licence numbers
     */
    static final Pattern LICENCE_PATTERN = Pattern.compile("B7A7([1-9])8$");
    /**
     * pattern to match valid mock contact identifiers
     */
    static final Pattern CONTACT_ID_PATTERN = Pattern.compile("^contact-identifier-([1-9])$");

    /**
     * Utility class constructor
     */
    private DynamicsMockData() {
    }

    /**
     * Retrieve a mock data for the given user index
     *
     * @param index a user index - should be between 1 and 9
     * @return an {@link Entry} representing the mock data for the given user
     */
    public static Entry get(@Range(min = 1, max = 9) final int index) {
        if (index < 1 || index > 9) {
            throw new RuntimeException("Dynamics test user index must be between 1-9");
        }

        return new Entry() {
            @Override
            public String getContactId() {
                return "contact-identifier-" + index;
            }

            @Override
            public String getPostcode() {
                return "WA4 " + index + "HT";
            }

            @Override
            public String getPermission() {
                return "00081019-1WS3JP4-B7A7" + index + "8";
            }
        };
    }


    /**
     * Dynamics mock data entry
     */
    public interface Entry {
        /**
         * @return the user's contact identifier
         */
        String getContactId();

        /**
         * @return the user's postcode
         */
        String getPostcode();

        /**
         * @return the user's full permission number
         */
        String getPermission();
    }
}
