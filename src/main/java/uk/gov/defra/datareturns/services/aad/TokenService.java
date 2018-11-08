package uk.gov.defra.datareturns.services.aad;

/**
 * Service to retrieve access token from Azure active directory
 *
 * @author Graham Willis
 */
public interface TokenService {
    /**
     * Get the token for a given AAD user - to authenticate the fish movements team
     *
     * @param username the user credentials username
     * @param password the user credentials password
     * @return the token for a given AAD user - to authenticate the fish movements team
     */
    String getTokenForUserIdentity(String username, String password);
}
