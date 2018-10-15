package uk.gov.defra.datareturns.data.model.licences;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data used for authentication and licence lookup when the API
 * is on mock-mode i.e. not using the CRM
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "mock")
public final class MockLicenceData {
    public static final Map<String, Licence> LICENCES;

    private MockLicenceData() { }

    static {
        final Map<String, Licence> temp = new HashMap<>();
        log.info("The API is running in mock mode");

        for (int i = 1; i <= 8; i++) {
            final Licence licence = new Licence();

            final String permission = String.format("B7A7%d8", i);
            final String postcode = String.format("WA4 %dHT", i);

            licence.setLicenceNumber(permission);
            final Contact contact = new Contact();

            contact.setPostcode(postcode);
            contact.setId(String.format("%s%d", "contact-identifier-", i));

            licence.setContact(contact);
            temp.put(permission, licence);

            log.info("Licence: " + licence.getLicenceNumber() + ", using postcode " + licence.getContact().getPostcode());
        }
        LICENCES = Collections.unmodifiableMap(temp);
    }
}
