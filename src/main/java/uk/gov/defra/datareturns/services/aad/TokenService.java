package uk.gov.defra.datareturns.services.aad;

/**
 * Service to retrieve access token from Azure active directory
 *
 * @author Graham Willis
 */
public interface TokenService {
    /**
     * Get the system (server-to-server) token
     * @return
     */
    String getToken();

    /**
     * Get the token for a given AAD user - to authenticate the fish movements team
     * @return
     */
    String getTokenForUserIdentity(String username, String password);
}
