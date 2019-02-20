package uk.gov.defra.datareturns.test.init.licence;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.gov.defra.datareturns.RcrApi;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Test service initialisation
 */
@Slf4j
public class InitialisationTests {
    @Test
    public void testStartup() {
        final int port = findAvailablePort();
        final String portArg = "--server.port=" + port;
        RcrApi.main(new String[] {
                "--spring.profiles.active=h2",
                "--spring.application.admin.jmx-name=org.springframework.boot:type=Admin,name=InitialisationTest",
                portArg
        });
        RestAssured.given().get("http://localhost:" + port + "/api").then().statusCode(200);
    }

    private int findAvailablePort() {
        try (final ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            throw new RuntimeException("Unable to find available port for test.");
        }
    }
}
