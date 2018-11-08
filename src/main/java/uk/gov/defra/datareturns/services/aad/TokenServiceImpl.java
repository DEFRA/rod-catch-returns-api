package uk.gov.defra.datareturns.services.aad;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;
import uk.gov.defra.datareturns.config.AADConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service to retrieve access token from Azure active directory
 *
 * @author Graham Willis
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
public class TokenServiceImpl implements TokenService {

    @Inject
    private final TokenServiceImpl proxy = null;

    private final AADConfiguration aadConfiguration;
    private final URL resource;
    private final URL tokenPath;

    @Inject
    public TokenServiceImpl(final AADConfiguration aadConfiguration, final DynamicsConfiguration.Endpoint endpoint) {
        this.aadConfiguration = aadConfiguration;
        this.resource = endpoint.getUrl();
        this.tokenPath = aadConfiguration.getTenantedLoginUrl();
    }

    @Override
    @Cacheable(cacheNames = "crm-auth-token-identity")
    public String getTokenForUserIdentity(final String username, final String password) {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            log.debug("Attempting to fetch user identity AAD token from " + tokenPath);

            final String clientId = aadConfiguration.getIdentityClientId();
            final AuthenticationContext context = new AuthenticationContext(tokenPath.toString(), true, service);

            // Attempt to acquire a token and fire the callback defined below
            final Future<AuthenticationResult> future = context.acquireToken(resource.toString(), clientId, username, password,
                    new IdentityTokenCallback(username, password));

            // Return the token as a string
            final AuthenticationResult result = future.get();

            if (result != null) {
                return result.getAccessToken();
            }
        } catch (final IOException | InterruptedException | ExecutionException e) {
            log.error("Error fetching identity token", e);
        } finally {
            service.shutdown();
        }
        return null;
    }

    /**
     * Evict user identity tokens from the cache
     *
     * @param username - the stored username
     */
    @CacheEvict(cacheNames = "crm-auth-token-identity")
    public void evictIdentityToken(final String username, final String password) {
        log.debug("Evicting AAD token from cache for user: " + username);
    }

    /**
     * Token acquire callback
     */
    @RequiredArgsConstructor
    private class IdentityTokenCallback implements AuthenticationCallback<AuthenticationResult> {
        private final String username;
        private final String password;

        public void onSuccess(final AuthenticationResult result) {

            // Success: execute a timer to evict the token from the cache before it expires
            final long seconds = result.getExpiresAfter();
            log.debug("AAD identity token acquired successfully: expires in " + seconds + " seconds");

            // Log the token in debug mode
            log.debug("Bearer " + result.getAccessToken());
            final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            // Remove the token 1 minute before it is due to expire
            final long delay = Math.max(seconds - 60, 0);
            executor.schedule(() -> proxy.evictIdentityToken(username, password), delay, TimeUnit.SECONDS);
            executor.shutdown();
        }

        public void onFailure(final Throwable throwable) {
            // Don't log authentication failures
            if (throwable.getMessage().contains("\"error\":\"invalid_grant\"")) {
                return;
            }
            log.error("AAD identity token acquisition failed", throwable);
        }
    }
}
