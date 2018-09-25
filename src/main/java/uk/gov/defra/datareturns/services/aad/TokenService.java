package uk.gov.defra.datareturns.services.aad;

/**
 * Service to retrieve access token from Azure active directory
 *
 * @author Graham Willis
 */
public interface TokenService {
    String getToken();
}
