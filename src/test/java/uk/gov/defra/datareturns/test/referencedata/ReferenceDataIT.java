package uk.gov.defra.datareturns.test.referencedata;

import io.restassured.response.ExtractableResponse;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithAdminUser;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.getEntity;

/**
 * Integration tests submission-level property validation
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@WithAdminUser
@Slf4j
public class ReferenceDataIT {
    @Test
    public void testRegionCatchments() {
        final ExtractableResponse<?> r = getEntity("/regions/1")
                .statusCode(200)
                .body("_links.catchments.href", Matchers.notNullValue())
                .extract();
        final String catchmentCollectionUrl = r.jsonPath().getString("_links.catchments.href");
        getEntity(catchmentCollectionUrl).statusCode(200).body("_embedded.catchments", Matchers.hasSize(Matchers.greaterThan(0)));
    }

    @Test
    public void testCatchmentRivers() {
        final ExtractableResponse<?> r = getEntity("/catchments/1")
                .statusCode(200)
                .body("_links.rivers.href", Matchers.notNullValue())
                .extract();
        final String catchmentCollectionUrl = r.jsonPath().getString("_links.rivers.href");
        getEntity(catchmentCollectionUrl).statusCode(200).body("_embedded.rivers", Matchers.hasSize(Matchers.greaterThan(0)));
    }
}
