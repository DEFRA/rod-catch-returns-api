package uk.gov.defra.datareturns.services.aad;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.aad.adal4j.AuthenticationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.AADConfiguration;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to retrieve access token from Azure active directory
 *
 * @author Graham Willis
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
public class TokenServiceImpl implements TokenService {
    /**
     * how early should a cached access token be refreshed
     */
    private static final long PREEMPTIVE_REFRESH = 10000;
    /**
     * cache service
     */
    private final TokenServiceCache cache;

    @Override
    @NonNull
    public String getTokenForUserIdentity(final String username, final String password) {
        AuthenticationResult result = cache.getToken(username, password);
        if (result == null || new Date(System.currentTimeMillis() - PREEMPTIVE_REFRESH).after(result.getExpiresOnDate())) {
            result = cache.updateToken(username, password);
        }
        return result.getAccessToken();
    }

    @Service
    @RequiredArgsConstructor
    @ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
    public static class TokenServiceCache {
        private final AADConfiguration aadConfiguration;
        private final DynamicsConfiguration.Endpoint endpoint;

        @CachePut(cacheNames = "crm-auth-token-identity")
        public AuthenticationResult updateToken(final String username, final String password) {
            final ExecutorService service = Executors.newSingleThreadExecutor();
            try {
                final AuthenticationContext context = new AuthenticationContext(aadConfiguration.getAuthority().toString(), true, service);
                return context.acquireToken(endpoint.getUrl().toString(), aadConfiguration.getIdentityClientId(), username, password, null).get();
            } catch (final Exception ex) {
                // handle adal4j AuthenticationExceptions and convert to spring authentication exceptions as required.
                // sadly the only way to do this is by looking at the exception message itself.
                if (ex.getCause() instanceof AuthenticationException) {
                    final AuthenticationException authEx = (AuthenticationException) ex.getCause();
                    if (authEx.getMessage().contains("ID3242: The security token could not be authenticated or authorized")) {
                        // adfs returns a 500 response (?!) with a soap envelope on authentication failure with a valid domain
                        throw new BadCredentialsException("AAD authentication failed - no identity was found for the given credentials.", authEx);
                    } else if (authEx.getMessage().contains("AADSTS90002: Tenant not found.")) { // domain specified but not recognised.
                        throw new BadCredentialsException("AAD authentication failed - invalid domain", authEx);
                    } else if (authEx.getMessage().contains("AADSTS50034: The user account does not exist")) { // no domain specified in username
                        throw new BadCredentialsException("AAD authentication failed - the user account does not exist in the directory.", authEx);
                    }
                }
                throw new AuthenticationServiceException("Error fetching identity token", ex);
            } finally {
                service.shutdown();
            }
        }

        @Cacheable(cacheNames = "crm-auth-token-identity")
        public AuthenticationResult getToken(final String username, final String password) {
            return null;
        }
    }
}
