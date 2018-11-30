package uk.gov.defra.datareturns.services.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.datareturns.services.aad.MockTokenServiceImpl;
import uk.gov.defra.datareturns.services.crm.entity.CrmRoles;
import uk.gov.defra.datareturns.services.crm.entity.CrmLicence;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

/**
 * Mock dynamics server
 * <p>
 * Provides functionality to bind to spring {@link RestTemplate}'s to emulate the functionality exposed by Dynamics for RCR.
 * This enables the API to be started in a local "mock" mode with known test data.
 *
 * @author Sam Gardner-Dell
 * @see uk.gov.defra.datareturns.config.DynamicsConfiguration
 */
@Slf4j
public final class DynamicsMockServer {
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern
            .compile("^Bearer " + MockTokenServiceImpl.MOCK_BEARER_TOKEN + "(?<responseCode>\\d{3})$");
    private static final ObjectMapper MAPPER = Jackson2ObjectMapperBuilder.json().build();

    private DynamicsMockServer() {
    }

    private static void setupCrmMock(final MockRestServiceServer restServiceServer, final HttpMethod method, final String apiCallPath,
                                     final ResponseCreator responseCreator) {
        restServiceServer.expect(ExpectedCount.manyTimes(), MockRestRequestMatchers.requestTo(Matchers.endsWith(apiCallPath)))
                .andExpect(MockRestRequestMatchers.method(method))
                .andRespond(responseCreator);
    }

    private static void setupGetContactByLicenceNumberMock(final MockRestServiceServer restServiceServer) {
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_GetContactByLicenceAndPostcode", (request) -> {
            final CrmLicence.QueryParams requestBody = readRequestBody(request, CrmLicence.QueryParams.class);
            final CrmLicence responseBody = new CrmLicence();
            final DynamicsMockData.Entry entry = DynamicsMockData.get(requestBody.getPermissionNumber());
            if (entry != null && equalsIgnoreCase(deleteWhitespace(requestBody.getPostcode()), deleteWhitespace(entry.getPostcode()))) {
                responseBody.setId(entry.getContactId());
                responseBody.setPermissionNumber(entry.getPermission());
                responseBody.setPostcode(entry.getPostcode());
            }
            return respond(HttpStatus.OK, responseBody);
        });
    }

    private static void setupCreateRCRActivityMock(final MockRestServiceServer restServiceServer) {
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_CreateRCRActivity",
                (request) -> new MockClientHttpResponse(new byte[] {}, HttpStatus.CREATED));
    }

    private static void setupUpdateRCRActivityMock(final MockRestServiceServer restServiceServer) {
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_UpdateRCRActivity",
                (request) -> new MockClientHttpResponse(new byte[] {}, HttpStatus.CREATED));
    }

    private static void setupGetRcrRolesByUserMock(final MockRestServiceServer restServiceServer) {
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_GetRcrRolesByUser", (request) -> {
            Assert.notNull(request, "request should not be null");
            final Matcher tokenMatcher = BEARER_TOKEN_PATTERN.matcher(Objects.toString(request.getHeaders().getFirst("Authorization")));
            CrmRoles responseBody = null;
            HttpStatus responseStatus = HttpStatus.FORBIDDEN;
            if (tokenMatcher.matches()) {
                responseStatus = HttpStatus.valueOf(Integer.parseInt(tokenMatcher.group("responseCode")));
                if (responseStatus.is2xxSuccessful()) {
                    responseBody = new CrmRoles();
                    responseBody.setRoles("RcrAdminUser");
                }
            }
            return respond(responseStatus, responseBody);
        });
    }

    private static MockClientHttpResponse respond(final HttpStatus status, final Object responseBody) throws IOException {
        final MockClientHttpResponse response = new MockClientHttpResponse(MAPPER.writeValueAsBytes(responseBody), status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response;
    }

    private static <T> T readRequestBody(final ClientHttpRequest request, final Class<T> entityClass) throws IOException {
        final MockClientHttpRequest mockRequest = (MockClientHttpRequest) request;
        return MAPPER.readValue(mockRequest.getBodyAsBytes(), entityClass);
    }

    public static MockRestServiceServer bindTo(final RestTemplate template) {
        log.info("DynamicsMockServer injected for " + template.toString());
        final MockRestServiceServer restServiceServer = MockRestServiceServer.bindTo(template).ignoreExpectOrder(true).build();
        setupGetContactByLicenceNumberMock(restServiceServer);
        setupCreateRCRActivityMock(restServiceServer);
        setupUpdateRCRActivityMock(restServiceServer);
        setupGetRcrRolesByUserMock(restServiceServer);
        return restServiceServer;
    }
}
