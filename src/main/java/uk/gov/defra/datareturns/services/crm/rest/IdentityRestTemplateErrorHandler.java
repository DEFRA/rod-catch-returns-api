package uk.gov.defra.datareturns.services.crm.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Rest template error handler for "identity" rest template calls (those that use a bearer token from an AD user's identity)
 *
 * @author Sam Gardner-Dell
 */
@Slf4j
public class IdentityRestTemplateErrorHandler extends DefaultResponseErrorHandler implements ResponseErrorHandler {
    @Override
    protected void handleError(final ClientHttpResponse response, final HttpStatus statusCode) {
        if (HttpStatus.FORBIDDEN.equals(statusCode)) {
            throw new InsufficientAuthenticationException("User not permitted to access the target Dynamics service.");
        }
        log.error("Unexpected response from Microsoft Dynamics using identity template. Status: " + statusCode.toString());
        throw new AuthenticationServiceException("Authentication service temporarily unavailable");
    }
}
