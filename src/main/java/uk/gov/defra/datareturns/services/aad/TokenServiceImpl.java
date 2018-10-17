package uk.gov.defra.datareturns.services.aad;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.AADConfiguration;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
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
    private URL tokenPath;

    @Inject
    public TokenServiceImpl(final AADConfiguration aadConfiguration, final DynamicsConfiguration dynamicsConfiguration) {
        this.aadConfiguration = aadConfiguration;

        final URI tenant = aadConfiguration.getTenant();
        final URL authority = aadConfiguration.getAuthority();
        resource = dynamicsConfiguration.getEndpoint();

        try {
            this.tokenPath = new URL(authority, tenant.toString());
        } catch (final MalformedURLException e) {
            log.error("Error in token specification: " + e.getMessage());
        }
    }

    private static AuthenticationContext createAuthenticationContext(final String authority, final ExecutorService service) throws IOException {
        final AuthenticationContext context = new AuthenticationContext(authority, true, service);
        // Use ProxySelector to determine correct proxy (the adalj sdk has a bug which doesn't automatically select the correct proxy.
        // Note, the .get(0) is safe because the list will always contain at least 1 proxy instance even if a proxy isn't used (Proxy.NO_PROXY)
        final Proxy proxy = ProxySelector.getDefault().select(URI.create(authority)).get(0);
        context.setProxy(proxy);
        return context;
    }

    @Override
    @Cacheable(cacheNames = "crm-auth-token")
    public String getToken() {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            log.debug("Attempting to fetch AAD token from " + tokenPath);

            final String clientId = aadConfiguration.getClientId();
            final String clientSecret = aadConfiguration.getClientSecret();

            // Generate a credentials payload from the clientId and secret
            final ClientCredential clientCredential = new ClientCredential(clientId, clientSecret);

            final AuthenticationContext context = createAuthenticationContext(tokenPath.toString(), service);

            // Attempt to acquire a token and fire the callback defined below
            final Future<AuthenticationResult> future = context.acquireToken(resource.toString(),
                    clientCredential, new SystemTokenCallback());

            // Return the token as a string
            final AuthenticationResult result = future.get();

            if (result != null) {
                return result.getAccessToken();
            }
        } catch (final IOException | InterruptedException | ExecutionException e) {
            log.error("Error fetching system token", e);
        } finally {
            service.shutdown();
        }
        return null;
    }

    @Override
    @Cacheable(cacheNames = "crm-auth-token-identity")
    public String getTokenForUserIdentity(final String username, final String password) {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            log.debug("Attempting to fetch user identity AAD token from " + tokenPath);

            final String clientId = aadConfiguration.getIdentityClientId();
            final AuthenticationContext context = createAuthenticationContext(tokenPath.toString(), service);

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
     * Evict the token from the cache for system token
     */
    @CacheEvict(cacheNames = "crm-auth-token", allEntries = true)
    public void evictToken() {
        log.debug("Evicting AAD token from cache");
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
    private class SystemTokenCallback implements AuthenticationCallback<AuthenticationResult> {
        public void onSuccess(final AuthenticationResult result) {

            // Success: execute a timer to evict the token from the cache before it expires
            final long seconds = result.getExpiresAfter();
            log.debug("AAD token acquired successfully: expires in " + seconds + " seconds");

            // Log the token in debug mode
            log.debug("Bearer " + result.getAccessToken());
            final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            // Remove the token 1 minute before it is due to expire
            final long delay = Math.max(seconds - 60, 0);
            executor.schedule(proxy::evictToken, delay, TimeUnit.SECONDS);
            executor.shutdown();
        }

        public void onFailure(final Throwable throwable) {
            log.error("AAD system token acquisition failed", throwable);
        }
    }

    /**
     * Token acquire callback
     */
    private class IdentityTokenCallback implements AuthenticationCallback<AuthenticationResult> {
        private final String username;
        private final String password;

        IdentityTokenCallback(final String username, final String password) {
            this.password = password;
            this.username = username;
        }

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
            log.error("AAD identity token acquisition failed", throwable);
        }
    }
}
