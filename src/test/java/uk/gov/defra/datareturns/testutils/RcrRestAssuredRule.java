package uk.gov.defra.datareturns.testutils;

import io.restassured.authentication.AuthenticationScheme;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.testcommons.restassured.RestAssuredRule;

import javax.inject.Inject;

@ConditionalOnWebApplication
@Service
@Slf4j
public class RcrRestAssuredRule extends RestAssuredRule {

    @Inject
    private AuthConfig authConfig;

    protected AuthenticationScheme getAuthentication() {
        final PreemptiveBasicAuthScheme authScheme = new PreemptiveBasicAuthScheme();
        authScheme.setUserName(authConfig.getUsername());
        authScheme.setPassword(authConfig.getPassword());
        log.info("Setting CRM authentication details: " + authConfig);
        return authScheme;
    }
}
