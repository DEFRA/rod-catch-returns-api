package uk.gov.defra.datareturns.testutils;

/**
 * Simple utility to generate a valid licence suffix for the mock dynamics implementation
 *
 * @author Sam Gardner-Dell
 */
public final class TestLicences {
    private TestLicences() {
    }

    /**
     * Retrieve a licence suffix for the given index
     *
     * @param index used to create the numeric part of the suffix
     * @return a licence suffix that will validate against the mock dynamics implementation
     */
    public static String getLicence(final int index) {
        return "A" + String.format("%5d", index);
    }
}
