package uk.gov.defra.datareturns.testutils;

import com.google.common.base.Charsets;
import io.restassured.RestAssured;
import io.restassured.authentication.AuthenticationScheme;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.filter.log.LogDetail;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@ConditionalOnWebApplication
@Service
@Slf4j
public class RcrRestAssuredRule extends ExternalResource {

    @Inject
    private EmbeddedWebApplicationContext context;

    @Inject
    private AuthConfig authConfig;

    @Override
    protected void before() {
        setupRestAssured();
    }

    @Override
    protected void after() {
    }

    private void setupRestAssured() {
        final int port = context.getEmbeddedServletContainer().getPort();
        log.info("Setting up Rest Assured on port {}", port);
        RestAssured.reset();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.authentication = getAuthentication();
        RestAssured.config().getEncoderConfig().defaultContentCharset(Charsets.UTF_8);
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
    }

    protected AuthenticationScheme getAuthentication() {
        PreemptiveBasicAuthScheme authScheme = new PreemptiveBasicAuthScheme();
        authScheme.setUserName(authConfig.getUsername());
        authScheme.setPassword(authConfig.getPassword());
        log.info("Setting CRM authentication details: " + authConfig);
        return authScheme;
    }
}
