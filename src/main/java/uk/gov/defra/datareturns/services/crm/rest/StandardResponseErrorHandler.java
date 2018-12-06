package uk.gov.defra.datareturns.services.crm.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.nio.charset.StandardCharsets;

/**
 * Common functionality for rest response error handlers
 */
@Slf4j
abstract class StandardResponseErrorHandler extends DefaultResponseErrorHandler implements ResponseErrorHandler {
    /**
     * Provide a human readable log message for the given response and status code
     *
     * @param response the {@link ClientHttpResponse} used to form the message
     * @param status   the {@link HttpStatus} used to form the message
     * @return a human readable log message
     */
    String toResponseLogMessage(final ClientHttpResponse response, final HttpStatus status) {
        final StringBuilder builder = new StringBuilder(System.lineSeparator());
        final String responseBody = new String(super.getResponseBody(response), StandardCharsets.UTF_8);
        builder.append("************************************************************").append(System.lineSeparator());
        builder.append("Status  : ").append(status).append(System.lineSeparator());
        builder.append("Headers : ").append(response.getHeaders()).append(System.lineSeparator());
        builder.append("Body    : ").append(responseBody).append(System.lineSeparator());
        builder.append("************************************************************").append(System.lineSeparator());
        return builder.toString();
    }
}
