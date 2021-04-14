package uk.gov.defra.datareturns.test.licence;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.testcommons.framework.RestAssuredTest;

import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.createEntity;
import static uk.gov.defra.datareturns.testutils.IntegrationTestUtils.getEntity;

/**
 * Integration tests for licence lookup functionality
 */
@RunWith(SpringRunner.class)
@RestAssuredTest
@Slf4j
public class LicenceLookupIT {
    @Test
    public void testLicenceLookupB7A111() {
        getEntity("/licence/B7A111?verification=WA4 1HT")
                .statusCode(HttpStatus.OK.value())
                .body("licenceNumber", Matchers.endsWith("B7A111"))
                .body("contact", Matchers.notNullValue())
                .body("contact.id", Matchers.equalTo("contact-identifier-111"))
                .body("contact.postcode", Matchers.equalTo("WA4 1HT"));
    }

    @Test
    public void testLicenceLookupB7A718() {
        getEntity("/licence/B7A718?verification=WA4 8HT")
                .statusCode(HttpStatus.OK.value())
                .body("licenceNumber", Matchers.endsWith("B7A718"))
                .body("contact", Matchers.notNullValue())
                .body("contact.id", Matchers.equalTo("contact-identifier-718"))
                .body("contact.postcode", Matchers.equalTo("WA4 8HT"));
    }

    @Test
    public void testLicenceLookupVerificationFailure() {
        getEntity("/licence/B7A718?verification=WA4 1HT").statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testLicenceLookupNotFound() {
        getEntity("/licence/notfound?verification=blah").statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testLicenceReadOnly() {
        createEntity("/licence/B7A718?verification=blah", "{}", r -> r.statusCode(HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    @Test
    public void testLFullLicenceLookupB7A111() {
        getEntity("/licence/full/00081019-1WS3JP4-B7A718")
                .statusCode(HttpStatus.OK.value())
                .body("licenceNumber", Matchers.endsWith("B7A718"))
                .body("contact", Matchers.notNullValue())
                .body("contact.id", Matchers.equalTo("contact-identifier-718"))
                .body("contact.fullName", Matchers.equalTo("Homer Simpson"));
    }

    @Test
    public void testLFullLicenceLookup6HB123() {
        getEntity("/licence/full/00081019-1WS3JP4-6HB123")
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void testFullLicenceLookupNumberInvalidCharacters() {
        getEntity("/licence/full/00081019`jas&^").statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testFullLicenceLookupLetterInvalidCharacters() {
        getEntity("/licence/full/**SGHSG").statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testFullLicenceLookupNotFound() {
        getEntity("/licence/full/00081019").statusCode(HttpStatus.FORBIDDEN.value());
    }

}
