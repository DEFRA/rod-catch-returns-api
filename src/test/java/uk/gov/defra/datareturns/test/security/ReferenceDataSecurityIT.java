package uk.gov.defra.datareturns.test.security;

import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.IntegrationTestUtils;
import uk.gov.defra.datareturns.testutils.WithAdminUser;
import uk.gov.defra.datareturns.testutils.WithEndUser;
import uk.gov.defra.datareturns.testutils.WithInvalidAdminPassword;
import uk.gov.defra.datareturns.testutils.WithInvalidEndUserPassword;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static uk.gov.defra.datareturns.testutils.ReferenceDataITUtils.getCatchmentJson;
import static uk.gov.defra.datareturns.testutils.ReferenceDataITUtils.getMethodJson;
import static uk.gov.defra.datareturns.testutils.ReferenceDataITUtils.getRegionJson;
import static uk.gov.defra.datareturns.testutils.ReferenceDataITUtils.getRiverJson;
import static uk.gov.defra.datareturns.testutils.ReferenceDataITUtils.getSpeciesJson;

/**
 * Security integration tests for reference data
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@Slf4j
public class ReferenceDataSecurityIT {
    private static final Map<String, Supplier<String>> REFERENCE_DATA_MAP = new LinkedHashMap<>();

    static {
        REFERENCE_DATA_MAP.put("/methods", () -> getMethodJson(RandomStringUtils.randomAlphabetic(10), false));
        REFERENCE_DATA_MAP.put("/species", () -> getSpeciesJson(RandomStringUtils.randomAlphabetic(10), BigDecimal.ONE));
        REFERENCE_DATA_MAP.put("/regions", () -> getRegionJson(RandomStringUtils.randomAlphabetic(10)));
        REFERENCE_DATA_MAP.put("/catchments", () -> getCatchmentJson(RandomStringUtils.randomAlphabetic(10), "regions/1"));
        REFERENCE_DATA_MAP.put("/rivers", () -> getRiverJson(RandomStringUtils.randomAlphabetic(10), "catchments/1", false));
    }

    private static void testReferenceDataWriteAccess(final Consumer<ValidatableResponse> responseAssertions) {
        REFERENCE_DATA_MAP.forEach((k, v) -> IntegrationTestUtils.createEntity(k, v.get(), responseAssertions));
    }

    @Test
    @WithAdminUser
    public void testAdminCanWriteReferenceData() {
        testReferenceDataWriteAccess((r) -> r.statusCode(HttpStatus.CREATED.value()));
    }

    @Test
    @WithInvalidAdminPassword
    public void testUnauthorisedAdminBlocked() {
        testReferenceDataWriteAccess((r) -> r.statusCode(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @WithEndUser
    public void testEndUserCannotWriteReferenceData() {
        testReferenceDataWriteAccess((r) -> r.statusCode(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @WithInvalidEndUserPassword
    public void testUnauthorisedEndUserBlocked() {
        testReferenceDataWriteAccess((r) -> r.statusCode(HttpStatus.UNAUTHORIZED.value()));
    }
}
