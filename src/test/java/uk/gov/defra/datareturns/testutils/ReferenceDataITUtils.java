package uk.gov.defra.datareturns.testutils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.fromJson;


/**
 * Test utilities for reference data
 *
 * @author Sam Gardner-Dell
 */
public final class ReferenceDataITUtils {

    private ReferenceDataITUtils() {
    }

    public static String getRegionJson(final String name) {
        return fromJson("/data/templates/region.json.template", Collections.singletonMap("NAME", name));
    }


    public static String getCatchmentJson(final String name, final String regionUri) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("NAME", name);
        replacements.put("REGION_URI", regionUri);
        return fromJson("/data/templates/catchment.json.template", replacements);
    }

    public static String getRiverJson(final String name, final String catchmentUri, final boolean internal) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("NAME", name);
        replacements.put("CATCHMENT_URI", catchmentUri);
        replacements.put("INTERNAL", internal);
        return fromJson("/data/templates/river.json.template", replacements);
    }


    public static String getMethodJson(final String name, final boolean internal) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("NAME", name);
        replacements.put("INTERNAL", internal);
        return fromJson("/data/templates/method.json.template", replacements);
    }

    public static String getSpeciesJson(final String name, final BigDecimal smallCatchMass) {
        final Map<String, Object> replacements = new HashMap<>();
        replacements.put("NAME", name);
        replacements.put("SMALL_CATCH_MASS", smallCatchMass.toString());
        return fromJson("/data/templates/species.json.template", replacements);
    }
}
