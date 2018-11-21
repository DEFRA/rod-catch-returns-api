package uk.gov.defra.datareturns.services.crm.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;

/**
 * Rest template error handler for dynamics calls using client credentials flow. Handles error responses from the oauth2 token endpoint.
 *
 * @author Sam Gardner-Dell
 */
@Slf4j
public class ClientRestTemplateOAuth2ErrorHandler extends DefaultResponseErrorHandler implements ResponseErrorHandler {
    @Override
    protected void handleError(final ClientHttpResponse response, final HttpStatus statusCode) {
        log.error("Unexpected response attempting to obtain token from provider using client credentials grant. Status: " + statusCode.toString());
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service temporarily unavailable");
    }
}
