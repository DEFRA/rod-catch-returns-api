package uk.gov.defra.datareturns.test.licence;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;
import uk.gov.defra.datareturns.testutils.WithEndUser;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.createEntity;
import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.getEntity;

/**
 * Integration tests for licence lookup functionality
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@WithEndUser
@Slf4j
public class LicenceLookupIT {
    @Test
    public void testLicenceLookupB7A111() {
        getEntity("/licence/B7A111")
                .body("licenceNumber", Matchers.endsWith("B7A111"))
                .body("contact", Matchers.notNullValue())
                .body("contact.id", Matchers.equalTo("contact-identifier-111"))
                .body("contact.postcode", Matchers.equalTo("WA4 1HT"));
    }

    @Test
    public void testLicenceLookupB7A718() {
        getEntity("/licence/B7A718")
                .body("licenceNumber", Matchers.endsWith("B7A718"))
                .body("contact", Matchers.notNullValue())
                .body("contact.id", Matchers.equalTo("contact-identifier-718"))
                .body("contact.postcode", Matchers.equalTo("WA4 8HT"));
    }

    @Test
    public void testLicenceLookupNotFound() {
        getEntity("/licence/notfound").statusCode(404);
    }

    @Test
    public void testLicenceReadOnly() {
        createEntity("/licence/B7A718", "{}", r -> r.statusCode(405));
    }
}
