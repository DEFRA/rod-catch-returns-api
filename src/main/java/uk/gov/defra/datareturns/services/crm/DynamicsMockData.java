package uk.gov.defra.datareturns.services.crm;

import java.util.regex.Matcher;
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
    public static final Pattern LICENCE_PATTERN = Pattern.compile("(?<suffix>.*\\D(?<index>\\d+))$");
    /**
     * pattern to match valid mock contact identifiers
     */
    public static final Pattern CONTACT_ID_PATTERN = Pattern.compile("^contact-identifier-(\\d+)$");

    /**
     * Utility class constructor
     */
    private DynamicsMockData() {
    }

    /**
     * Retrieve a mock data for the given 6 digit licence suffix
     *
     * @param permissionNumber the 6 digit licence suffix
     * @return an {@link Entry} representing the mock data for the given user
     */
    public static Entry get(final String permissionNumber) {
        final Matcher licenceMatcher = DynamicsMockData.LICENCE_PATTERN.matcher(permissionNumber);
        Entry result = null;
        if (licenceMatcher.matches()) {
            final String suffix = licenceMatcher.group("suffix");
            final int index = Integer.parseInt(licenceMatcher.group("index"));
            result = new Entry() {
                @Override
                public String getContactId() {
                    return "contact-identifier-" + index;
                }

                @Override
                public String getPostcode() {
                    return "WA4 " + (index % 10) + "HT";
                }

                @Override
                public String getPermission() {
                    return "00081019-1WS3JP4-" + suffix;
                }
            };
        }
        return result;
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
