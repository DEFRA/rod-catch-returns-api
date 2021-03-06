package uk.gov.defra.datareturns.test.restclient;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.defra.datareturns.services.crm.rest.ClientRestTemplateErrorHandler;
import uk.gov.defra.datareturns.services.crm.rest.ClientRestTemplateOAuth2ErrorHandler;
import uk.gov.defra.datareturns.services.crm.rest.IdentityRestTemplateErrorHandler;

/**
 * Unit tests for client rest template error handling
 *
 * @author Sam Gardner-Dell
 */
@RunWith(SpringRunner.class)
@Slf4j
public class RestTemplateErrorHandlingTests {
    @Rule
    public final OutputCapture capture = new OutputCapture();

    @Test
    public void testClientRestTemplateOAuth2ErrorHandler() {
        try {
            final ClientRestTemplateOAuth2ErrorHandler handler = new ClientRestTemplateOAuth2ErrorHandler();
            handler.handleError(new MockClientHttpResponse("".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (final Throwable t) {
            Assertions.assertThat(t).isInstanceOf(ResponseStatusException.class);
            Assertions.assertThat(capture.toString())
                    .contains("Unexpected response attempting to obtain token from provider using client credentials grant.")
                    .contains("500 INTERNAL_SERVER_ERROR");
        }
    }

    @Test
    public void testClientRestTemplateDynamicsErrorHandler() {
        try {
            final ClientRestTemplateErrorHandler handler = new ClientRestTemplateErrorHandler();
            handler.handleError(new MockClientHttpResponse("".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (final Throwable t) {
            Assertions.assertThat(t).isInstanceOf(ResponseStatusException.class);
            Assertions.assertThat(capture.toString())
                    .contains("Unexpected response from Microsoft Dynamics using client template.")
                    .contains("500 INTERNAL_SERVER_ERROR");
        }
    }

    @Test
    public void testIdentityRestTemplateInternalErrorHandling() {
        try {
            final IdentityRestTemplateErrorHandler handler = new IdentityRestTemplateErrorHandler();
            handler.handleError(new MockClientHttpResponse("".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (final AuthenticationServiceException e) {
            Assertions.assertThat(capture.toString())
                    .contains("Unexpected response from Microsoft Dynamics using identity template.")
                    .contains("500 INTERNAL_SERVER_ERROR");
        } catch (final Throwable t) {
            Assertions.fail("Unexpected exception thrown by IdentityRestTemplateErrorHandler", t);
        }
    }

    @Test
    public void testIdentityRestTemplateForbiddenErrorHandling() {
        try {
            final IdentityRestTemplateErrorHandler handler = new IdentityRestTemplateErrorHandler();
            handler.handleError(new MockClientHttpResponse("".getBytes(), HttpStatus.FORBIDDEN));
        } catch (final InsufficientAuthenticationException e) {
            Assertions.assertThat(capture.toString()).doesNotContain("User not permitted to access the target Dynamics service.");
        } catch (final Throwable t) {
            Assertions.fail("Unexpected exception thrown by IdentityRestTemplateErrorHandler", t);
        }
    }
}
