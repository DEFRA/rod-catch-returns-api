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
import org.springframework.web.client.RestTemplate;
import uk.gov.defra.datareturns.services.crm.entity.CrmActivity;
import uk.gov.defra.datareturns.services.crm.entity.CrmIdentity;
import uk.gov.defra.datareturns.services.crm.entity.CrmLicence;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;

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
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_GetContactByLicenseNumber", (request) -> {
            final CrmLicence.LicenceQuery.Query requestBody = readRequestBody(request, CrmLicence.LicenceQuery.Query.class);
            final CrmLicence responseBody = new CrmLicence();
            final DynamicsMockData.Entry entry = DynamicsMockData.get(requestBody.getPermissionNumber());
            if (entry != null) {
                responseBody.setId(entry.getContactId());
                responseBody.setPermissionNumber(entry.getPermission());
                responseBody.setPostcode(entry.getPostcode());
            } else {
                responseBody.setReturnStatus("error");
                responseBody.setErrorMessage("Unable to retrieve permission");
            }
            return respond(HttpStatus.OK, responseBody);
        });
    }

    private static void setupCreateRCRActivityMock(final MockRestServiceServer restServiceServer) {
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_CreateRCRActivity", (request) -> {
            final CrmActivity.CreateActivity.Query requestBody = readRequestBody(request, CrmActivity.CreateActivity.Query.class);
            final Matcher contactMatcher = DynamicsMockData.CONTACT_ID_PATTERN.matcher(requestBody.getContactId());
            final CrmActivity responseBody = new CrmActivity();

            if (contactMatcher.matches() && requestBody.getSeason() >= 2018 && requestBody.getStatus() != null) {
                responseBody.setId(requestBody.getContactId() + UUID.randomUUID().toString());
            } else {
                responseBody.setReturnStatus("error");
                responseBody.setErrorMessage("Invalid contact id, season or status");
            }
            return respond(HttpStatus.CREATED, responseBody);
        });
    }

    private static void setupUpdateRCRActivityMock(final MockRestServiceServer restServiceServer) {
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_UpdateRCRActivity", (request) -> {
            final CrmActivity.UpdateActivity.Query requestBody = readRequestBody(request, CrmActivity.UpdateActivity.Query.class);
            final Matcher contactMatcher = DynamicsMockData.CONTACT_ID_PATTERN.matcher(requestBody.getContactId());
            final CrmActivity responseBody = new CrmActivity();

            if (contactMatcher.matches() && requestBody.getSeason() >= 2018 && requestBody.getStatus() != null) {
                responseBody.setId(requestBody.getContactId() + UUID.randomUUID().toString());
            } else {
                responseBody.setReturnStatus("error");
                responseBody.setErrorMessage("Invalid contact id, season or status");
            }
            return respond(HttpStatus.OK, responseBody);
        });
    }

    private static void setupGetRcrRolesByUserMock(final MockRestServiceServer restServiceServer) {
        setupCrmMock(restServiceServer, HttpMethod.POST, "/api/data/v9.0/defra_GetRcrRolesByUser", (request) -> {
            final String expected = "Bearer " + MockCrmLookupService.MOCK_BEARER_TOKEN;
            final String authHeader = request.getHeaders().getFirst("Authorization");
            final CrmIdentity responseBody = new CrmIdentity();
            if (expected.equals(authHeader)) {
                responseBody.setRoles("RcrAdminUser");
                return respond(HttpStatus.OK, responseBody);
            }
            return respond(HttpStatus.FORBIDDEN, null);
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
